import unittest
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager
from helpers.login_page import LoginPage
from time import sleep

class LoginTest(unittest.TestCase):
    def setUp(self):
        options = Options()
        # options.add_argument("--headless")
        self.driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)
        self.login_page = LoginPage(self.driver)

    def test_valid_login(self):
        self.login_page.login("admin")
        current_url = self.login_page.wait_for_login_success()
        self.assertIn("home", current_url)

        sleep(2)

    def tearDown(self):
        self.driver.quit()