#!/usr/bin/env python3
"""
Sahibinden.com Scraper using undetected-chromedriver
Bypasses Cloudflare protection and extracts listing data
"""

import sys
import json
import time
import random
import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

def random_sleep(min_seconds, max_seconds):
    time.sleep(random.uniform(min_seconds, max_seconds))

def handle_cloudflare(driver):
    """
    Attempts to handle Cloudflare Turnstile/Challenge and 'Devam Et' screen
    """
    try:
        # Loop for a short duration to catch the button if it appears late
        start_time = time.time()
        while time.time() - start_time < 15:
            page_source = driver.page_source
            
            # Check for "Devam Et" button (Sahibinden specific)
            if "Devam Et" in page_source:
                print("   'Devam Et' text detected, looking for button...", file=sys.stderr)
                try:
                    # Try multiple selectors for the button
                    buttons = driver.find_elements(By.XPATH, "//button[contains(text(), 'Devam Et')] | //a[contains(text(), 'Devam Et')] | //input[@value='Devam Et']")
                    if buttons:
                        print("   Clicking 'Devam Et' button...", file=sys.stderr)
                        buttons[0].click()
                        random_sleep(3, 5)
                        return True
                except Exception as e:
                    print(f"   Error clicking 'Devam Et': {e}", file=sys.stderr)

            # Check for standard Cloudflare challenge
            if "Just a moment" in page_source or "Bir dakika" in page_source or "Tarayıcınızı kontrol ediyoruz" in page_source:
                print("⏳ Cloudflare challenge detected...", file=sys.stderr)
                
                # Try to find and click the challenge checkbox (iframe)
                iframes = driver.find_elements(By.TAG_NAME, "iframe")
                for iframe in iframes:
                    try:
                        src = iframe.get_attribute("src")
                        if src and ("cloudflare" in src or "turnstile" in src):
                            driver.switch_to.frame(iframe)
                            checkbox = WebDriverWait(driver, 2).until(
                                EC.element_to_be_clickable((By.CSS_SELECTOR, "input[type='checkbox'], .ctp-checkbox-label, #challenge-stage"))
                            )
                            if checkbox:
                                print("   Clicking Cloudflare checkbox...", file=sys.stderr)
                                checkbox.click()
                                random_sleep(2, 4)
                            driver.switch_to.default_content()
                    except:
                        driver.switch_to.default_content()
            
            random_sleep(1, 2)
            
        return False
    except Exception as e:
        print(f"⚠️ Error handling Cloudflare: {e}", file=sys.stderr)
    return False

def scrape_sahibinden(url):
    """
    Scrape a single listing from sahibinden.com
    """
    options = uc.ChromeOptions()
    # options.add_argument('--headless')  # Keep headed for better bypass
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')
    options.add_argument('--disable-blink-features=AutomationControlled')
    options.add_argument('--lang=tr-TR')
    
    # Randomize window size
    width = random.randint(1200, 1600)
    height = random.randint(800, 1000)
    options.add_argument(f'--window-size={width},{height}')
    
    driver = uc.Chrome(options=options)
    
    try:
        # 1. Clear cookies (as requested)
        driver.delete_all_cookies()
        
        print(f"🌐 Navigating to: {url}", file=sys.stderr)
        driver.get(url)
        
        # Check if redirected to login page
        if "login" in driver.current_url:
            print("⚠️ Redirected to login page. Retrying with fresh cookies...", file=sys.stderr)
            random_sleep(5, 8)
            driver.delete_all_cookies()
            driver.get(url)
        
        # 2. Handle Cloudflare
        handle_cloudflare(driver)
        
        # Double check if we are still on a challenge page
        if "Tarayıcınızı kontrol ediyoruz" in driver.page_source:
             print("⚠️ Still on challenge page, waiting longer...", file=sys.stderr)
             random_sleep(10, 15)
        
        # 3. Extract data
        data = {
            "url": url,
            "baslik": None,
            "fiyat": None,
            "telefonNumarasi": None,
            "konum": None,
            "ilanTarihi": None,
            "aciklama": None,
            "ilanSahibi": None
        }
        
        # Title
        try:
            title_elem = WebDriverWait(driver, 10).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, "h1"))
            )
            data["baslik"] = title_elem.text.strip()
            print(f"✅ Title: {data['baslik']}", file=sys.stderr)
        except Exception:
            print(f"⚠️ Title not found (Page might be blocked)", file=sys.stderr)
        
        # Price
        try:
            price_elem = driver.find_element(By.CSS_SELECTOR, ".classifiedInfo h3")
            data["fiyat"] = price_elem.text.strip()
        except Exception: pass
        
        # Location
        try:
            location_elem = driver.find_element(By.CSS_SELECTOR, ".classifiedInfo h2")
            data["konum"] = location_elem.text.strip()
        except Exception: pass
        
        # Date
        try:
            # Try finding by label "İlan Tarihi"
            date_elem = driver.find_element(By.XPATH, "//strong[contains(text(), 'İlan Tarihi')]/following-sibling::span")
            data["ilanTarihi"] = date_elem.text.strip()
        except:
            try:
                # Fallback to previous selector
                date_elem = driver.find_element(By.CSS_SELECTOR, ".classifiedInfo em")
                data["ilanTarihi"] = date_elem.text.strip()
            except Exception: pass
        
        # Phone number (click to reveal)
        try:
            # Try multiple selectors for the button
            phone_button = None
            selectors = [
                (By.ID, "classifiedCallButton"),
                (By.CSS_SELECTOR, ".show-phone-number"),
                (By.XPATH, "//button[contains(text(), 'Telefon')]"),
                (By.XPATH, "//a[contains(@class, 'show-phone')]")
            ]
            
            for by, value in selectors:
                try:
                    phone_button = driver.find_element(by, value)
                    if phone_button:
                        print(f"   Found phone button with {value}", file=sys.stderr)
                        break
                except: pass
            
            if phone_button:
                driver.execute_script("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", phone_button)
                random_sleep(1, 2)
                phone_button.click()
                random_sleep(2, 3)
                
                # Try user provided XPath for data-content attribute (Most reliable)
                try:
                    print("   Looking for phone element with data-content...", file=sys.stderr)
                    # Wait for the element to have the attribute
                    phone_elem = WebDriverWait(driver, 5).until(
                        EC.presence_of_element_located((By.XPATH, "//*[@id='phoneInfoPart']//span[@data-content]"))
                    )
                    
                    # Extract various properties for debugging/verification as requested
                    data_content = phone_elem.get_attribute("data-content")
                    text_value = phone_elem.text
                    value_attr = phone_elem.get_attribute("value")
                    
                    print(f"   Debug - data-content: {data_content}", file=sys.stderr)
                    print(f"   Debug - text: {text_value}", file=sys.stderr)
                    print(f"   Debug - value: {value_attr}", file=sys.stderr)
                    
                    # Use data-content as the primary source
                    if data_content:
                        data["telefonNumarasi"] = data_content.strip()
                        print(f"✅ Phone (data-content): {data['telefonNumarasi']}", file=sys.stderr)
                    elif text_value:
                         # Fallback to text if data-content is empty (unlikely based on screenshot)
                        data["telefonNumarasi"] = text_value.strip()
                        print(f"✅ Phone (text fallback): {data['telefonNumarasi']}", file=sys.stderr)
                    else:
                        raise Exception("Both data-content and text are empty")
                        
                except Exception as e:
                    print(f"   First phone extraction failed: {e}", file=sys.stderr)
                    # Fallback to previous methods
                    try:
                        phone_elem = driver.find_element(By.XPATH, "//*[@class='dl-group']//dd")
                        data["telefonNumarasi"] = phone_elem.text.strip()
                        print(f"✅ Phone (Previous XPath): {data['telefonNumarasi']}", file=sys.stderr)
                    except:
                        try:
                            phone_elem = driver.find_element(By.CSS_SELECTOR, ".pretty-phone-part")
                            data["telefonNumarasi"] = phone_elem.text.strip().replace(" ", "")
                            print(f"✅ Phone (CSS): {data['telefonNumarasi']}", file=sys.stderr)
                        except Exception as e:
                            print(f"⚠️ Phone element extraction failed: {e}", file=sys.stderr)
            else:
                print("⚠️ Phone button not found", file=sys.stderr)
                
        except Exception as e:
            print(f"⚠️ Phone extraction failed: {e}", file=sys.stderr)
        
        return data
        
    except Exception as e:
        print(f"❌ Error during scraping: {e}", file=sys.stderr)
        return {
            "url": url,
            "error": str(e)
        }
    finally:
        driver.quit()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 sahibinden_scraper.py <url>", file=sys.stderr)
        sys.exit(1)
    
    url = sys.argv[1]
    result = scrape_sahibinden(url)
    
    # Output JSON to stdout
    print(json.dumps(result, ensure_ascii=False, indent=2))
