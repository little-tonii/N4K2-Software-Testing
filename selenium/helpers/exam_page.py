from time import sleep
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait, Select
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.keys import Keys
from selenium.common.exceptions import NoSuchElementException

class ExamPage:
    def __init__(self, driver):
        self.driver = driver
        self.url = "http://localhost:4200/user/dashboard"

    def open(self):
        self.driver.get(self.url)

    def select_test(self, test_name):
        WebDriverWait(self.driver, 10).until(
            EC.presence_of_all_elements_located((By.CSS_SELECTOR, "article"))
        )

        articles = self.driver.find_elements(By.CSS_SELECTOR, "article")

        for article in articles:
            try:
                title_element = article.find_element(By.CSS_SELECTOR, "h1 > a")
                title_text = title_element.text.strip()

                if title_text == test_name:
                    print(f"Joining test: {title_text}")
                    title_element.click()
                    sleep(1)
            except Exception as e:
                print(f"Error processing an article: {e}")

    def join_test(self):
        start_button = self.driver.find_element(By.XPATH, "//a[contains(text(), 'Start now') or contains(text(), 'Resume')]")
        old_url = self.driver.current_url
        start_button.click()
        WebDriverWait(self.driver, 10).until(lambda d: d.current_url != old_url)
        self.exam_id = old_url.split('/')[-1]
        sleep(2)


    def answer_questions(self, answer_indices):
        questions = self.driver.find_elements(By.XPATH, "//div[contains(@class, 'bg-gray-300') and contains(@class, 'rounded-md')]")

        answer_index = 0

        for i in range(len(questions)):
            try:
                question = self.driver.find_elements(By.XPATH, "//div[contains(@class, 'bg-gray-300') and contains(@class, 'rounded-md')]")[i]

                try:
                    index_text = question.find_element(By.XPATH, ".//span[contains(@class, 'font-bold')]").text.strip()
                except NoSuchElementException:
                    continue

                print(f"Answering {index_text}")

                inputs = question.find_elements(By.XPATH, ".//input[@type='radio' or @type='checkbox']")

                if answer_index < len(answer_indices):
                    selected_indices = answer_indices[answer_index]

                    for idx in selected_indices:
                        if 0 <= idx < len(inputs):
                            input_el = inputs[idx]
                            input_id = input_el.get_attribute("id")

                            label_xpath = f".//label[@for='{input_id}']"
                            label = question.find_element(By.XPATH, label_xpath)

                            self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", label)
                            self.driver.execute_script("arguments[0].click();", label)

                            print(f"Selected answer {idx + 1} for {index_text}")

                        else:
                            print(f"Index {idx} out of range for {index_text}")
                    answer_index += 1
                else:
                    print(f"No answer provided for {index_text}")

            except Exception as e:
                print(f"Could not process question #{i + 1}: {e}")

        submit_btn = self.driver.find_element(By.XPATH, "//button[contains(@class, 'bg-blue-500') and contains(text(), 'Submit')]")
        submit_btn.click()

        submit_btn = self.driver.find_element(By.XPATH, "//button[contains(@class, 'bg-gray-500') and contains(text(), 'Submit')]")
        submit_btn.click()

    def get_result(self):
        self.driver.get(f"http://localhost:4200/user/exams/{self.exam_id}/result")
        sleep(1)



