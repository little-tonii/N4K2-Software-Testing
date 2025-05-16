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

    def get_question_details(self, course, part, question_name):
        dropdown_element = self.driver.find_element(By.XPATH, "//select[contains(@class, 'appearance-none') and contains(@class, 'w-full')]")
        select = Select(dropdown_element)
        select.select_by_visible_text("Tất cả")

        dropdown_element = self.driver.find_element(By.NAME, "course")
        select = Select(dropdown_element)
        select.select_by_visible_text(course)

        sleep(1)

        dropdown_element = self.driver.find_element(By.NAME, "part")
        select = Select(dropdown_element)
        select.select_by_visible_text(part)

        print(question_name)

        question_element = WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located((By.XPATH, f"//div[p[text()=\"{question_name}\"]]"))
        )

        print("Question found. Clicking to open details page...")
        old_url = self.driver.current_url
        question_element.click()

        WebDriverWait(self.driver, 10).until(lambda d: d.current_url != old_url)
        print("Page opened.")


    def click_create_question(self):
        self.wait_until_ready()
        print("Current URL:", self.driver.current_url)
        create_btn = WebDriverWait(self.driver, 10).until(
            EC.element_to_be_clickable((By.XPATH, "//button[.//span[contains(text(),'Add new')]]"))
        )
        create_btn.click()

    def set_question_type(self, course, part, difficulty):
        dropdown_element = self.driver.find_element(By.NAME, "course")
        select = Select(dropdown_element)
        select.select_by_visible_text(course)

        sleep(1)

        dropdown_element = self.driver.find_element(By.NAME, "part")
        select = Select(dropdown_element)
        select.select_by_visible_text(part)

        dropdown_element = self.driver.find_element(By.XPATH, "//select[@formcontrolname='difficultyLevel']")
        select = Select(dropdown_element)
        select.select_by_visible_text(difficulty)

    def create_true_false_question(self,  question_text, answer_true=True):
        # Not a great code at all. Because the author doesn't set the ID for the radio button
        # So I have to use the ID.
        true_false_radio = self.driver.find_element(By.CSS_SELECTOR, "label[for='1']")
        true_false_radio.click()

        editor_div = self.driver.find_element(By.CSS_SELECTOR, "div.ck-editor__editable[contenteditable='true']")
        editor_div.send_keys(Keys.CONTROL + "a")
        editor_div.send_keys(Keys.BACKSPACE)
        editor_div.send_keys(question_text)

        if answer_true:
            self.driver.find_element(By.CSS_SELECTOR, "label[for='T']").click()
        else:
            self.driver.find_element(By.CSS_SELECTOR, "label[for='False']").click()

        self.driver.find_element(By.XPATH, "//button[@type='submit']").click()

    def create_multiple_choice_question(self, question_text, choices, correct_index):
        question_input = WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located((By.ID, "question_text"))
        )
        question_input.clear()
        question_input.send_keys(question_text)

        # Assume inputs for choices have IDs choice_0, choice_1, etc.
        for i, choice_text in enumerate(choices):
            choice_input = self.driver.find_element(By.ID, f"choice_{i}")
            choice_input.clear()
            choice_input.send_keys(choice_text)

        # Select correct answer radio button by index
        self.driver.find_element(By.ID, f"correct_choice_{correct_index}").click()

        self.driver.find_element(By.XPATH, "//button[contains(text(),'Submit')]").click()
