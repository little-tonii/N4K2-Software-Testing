import unittest
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager
from helpers.login_page import LoginPage
from helpers.exam_page import ExamPage
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait, Select
from time import sleep

class DoExamTest(unittest.TestCase):
    def setUp(self):
        options = Options()
        # options.add_argument("--headless")
        self.driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)
        self.login_page = LoginPage(self.driver)
        self.exam_page = ExamPage(self.driver)

    def test_do_exam(self):
        test_name = "test"
        answer_indices = [
            {1}, 
            {0}, 
            {0},
            {2},
            {0},
            {1},
            {0, 1},
            {2},
            {2},
        ]

        self.login_page.login("student")
        self.login_page.wait_for_login_success()
        self.exam_page.open()
        self.exam_page.select_test(test_name)

        test_name_element = self.driver.find_element(By.XPATH, f"//h2[text()='Title: {test_name}']")
        self.assertTrue(test_name_element.text == f'Title: {test_name}', f"Expected result: 'Title: {test_name}', but got: {test_name_element.text}")   

        self.exam_page.join_test()
        self.exam_page.answer_questions(answer_indices)
        self.exam_page.get_result()

        result = self.driver.find_element(By.XPATH, f"//div[text()='100/100']")
        self.assertTrue(result.text == f'100/100', f"Expected result: '100/100', but got: {result.text}") 

    def tearDown(self):
        self.driver.quit()