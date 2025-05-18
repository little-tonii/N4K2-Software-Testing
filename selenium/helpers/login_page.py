from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from const import USERS
import time

class LoginPage:
    def __init__(self, driver):
        self.driver = driver
        self.url = "http://localhost:4200/login"
        self.username_id = "username"
        self.password_id = "password"
        self.login_button_xpath = "//button[normalize-space()='Đăng nhập']"

    def open(self):
        self.driver.get(self.url)

    def login(self, username, password):
        self.open()

        time.sleep(1)

        self.driver.find_element(By.ID, self.username_id).send_keys(username)
        self.driver.find_element(By.ID, self.password_id).send_keys(password)
        self.driver.find_element(By.XPATH, self.login_button_xpath).click()

    def login(self, user_type):
        user = USERS.get(user_type)
        if not user:
            print(f"[ERROR] User type '{user_type}' not found!")
            return False
        
        self.open()
        self.driver.find_element(By.ID, self.username_id).send_keys(user["username"])
        self.driver.find_element(By.ID, self.password_id).send_keys(user["password"])
        self.driver.find_element(By.XPATH, self.login_button_xpath).click()
        print(f"[INFO] Logged in as {user['username']}")

    def wait_for_login_success(self, timeout=10):
        old_url = self.driver.current_url
        WebDriverWait(self.driver, timeout).until(lambda d: d.current_url != old_url)
        return self.driver.current_url