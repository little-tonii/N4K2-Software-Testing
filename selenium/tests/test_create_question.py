import unittest
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager
from helpers.login_page import LoginPage
from helpers.question_page import QuestionPage
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait, Select
from time import sleep

class CreateQuestionTests(unittest.TestCase):
    def setUp(self):
        options = Options()
        # options.add_argument("--headless")
        self.driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)
        self.login_page = LoginPage(self.driver)
        self.question_page = QuestionPage(self.driver)

    def test_create_true_false_question(self):
        course = "Professional Speaking"
        part = "Pronunciation"
        question_name = "Anh em minh cu the thoi hehehehe?"

        self.login_page.login("admin")
        self.login_page.wait_for_login_success()
        self.question_page.open()
        self.question_page.click_create_question()
        self.question_page.set_question_type(course, part, "Easy - 5pts")
        self.question_page.create_true_false_question(question_name, True)

        # Reset the page
        self.question_page.open()
        self.question_page.get_question_details(course, part, question_name)
        
        sleep(2)

        # The assert part is kinda useless since if the element is not found, it will throw an exception
        # and the test will fail anyway
        course_element = self.driver.find_element(By.XPATH, f"//option[contains(text(), \"{course}\")]")
        self.assertTrue(course_element.text == course, f"Expected course: {course}, but got: {course_element.text}")

        part_element = self.driver.find_element(By.XPATH, f"//option[contains(text(), \"{part}\")]")
        self.assertTrue(part_element.text == part, f"Expected part: {part}, but got: {part_element.text}")

        difficulty_dropdown = self.driver.find_element(
            By.XPATH, 
            "//label[contains(text(),'Độ khó')]/following-sibling::select"
        )

        select = Select(difficulty_dropdown)
        selected_option = select.first_selected_option.text.strip()

        self.assertTrue(selected_option == "Easy - 5pts", f"Expected difficulty: Easy - 5pts, but got: {selected_option}")
        
        question_type_element = self.driver.find_element(By.XPATH, f"//label[contains(text(), \"True/False\")]")
        self.assertTrue(question_type_element.text == "True/False", f"Expected question type: True/False, but got: {question_type_element.text}")

        question_name_element = self.driver.find_element(By.XPATH, f"//div[p[text()=\"{question_name}\"]]")
        self.assertTrue(question_name_element.text == question_name, f"Expected question name: {question_name}, but got: {question_name_element.text}")


    def test_create_multiple_choice_question(self):
        course = "Professional Speaking"
        part = "Pronunciation"
        question_name = "Ai là người gánh SQA"
        choices = ["Tonly", "Bình", "Đông", "Hiếu"]
        self.login_page.login("admin")
        self.login_page.wait_for_login_success()
        self.question_page.open()
        self.question_page.click_create_question()
        self.question_page.set_question_type(course, part, "Easy - 5pts")
        self.question_page.create_multiple_choice_question(question_name, choices, 1)

        # Reset the page
        self.question_page.open()
        self.question_page.get_question_details(course, part, question_name)
        
        sleep(2)

        course_element = self.driver.find_element(By.XPATH, f"//option[contains(text(), \"{course}\")]")
        self.assertTrue(course_element.text == course, f"Expected course: {course}, but got: {course_element.text}")

        part_element = self.driver.find_element(By.XPATH, f"//option[contains(text(), \"{part}\")]")
        self.assertTrue(part_element.text == part, f"Expected part: {part}, but got: {part_element.text}")

        difficulty_dropdown = self.driver.find_element(
            By.XPATH, 
            "//label[contains(text(),'Độ khó')]/following-sibling::select"
        )

        select = Select(difficulty_dropdown)
        selected_option = select.first_selected_option.text.strip()

        self.assertTrue(selected_option == "Easy - 5pts", f"Expected difficulty: Easy - 5pts, but got: {selected_option}")
        
        question_type_element = self.driver.find_element(By.XPATH, f"//label[contains(text(), \"Multiple Choice\")]")
        self.assertTrue(question_type_element.text == "Multiple Choice", f"Expected question type: Multiple Choice, but got: {question_type_element.text}")

        question_name_element = self.driver.find_element(By.XPATH, f"//div[p[text()=\"{question_name}\"]]")
        self.assertTrue(question_name_element.text == question_name, f"Expected question name: {question_name}, but got: {question_name_element.text}")        

        radio_inputs = self.driver.find_elements(By.CSS_SELECTOR, "input[type='radio'][name^='mcChoices']")

        for radio_input in radio_inputs:
            if radio_input.is_selected():
                label = radio_input.find_element(By.XPATH, "./following-sibling::label")
                print(f"Selected option: {label.text}")

    def tearDown(self):
        self.driver.quit()