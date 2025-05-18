from time import sleep
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait, Select
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.keys import Keys
from selenium.common.exceptions import NoSuchElementException

class QuestionPage:
    def __init__(self, driver):
        self.driver = driver
        self.url = "http://localhost:4200/admin/question-bank"

    def open(self):
        self.driver.get(self.url)
        
    def wait_until_ready(self):
        print("Waiting for question bank page to load...")
        WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located((By.XPATH, "//span[contains(., 'Add new')]"))
        )
        print("Question bank page loaded.")