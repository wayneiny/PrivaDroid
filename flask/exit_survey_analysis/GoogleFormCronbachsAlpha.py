import csv
import numpy as np
import json

GOOGLE_FORM_MTURK_RESPONSES_FILENAME_V1_50 = '/Users/weichengcao/ActivityBuddyBundle/privadroid/exit_survey_analysis/PrivaDroid_Exit_Survey_MTurk_GForm_Responses_V1_50.csv'
GOOGLE_FORM_MTURK_RESPONSES_FILENAME_V1_100 = '/Users/weichengcao/ActivityBuddyBundle/privadroid/exit_survey_analysis/PrivaDroid_Exit_Survey_MTurk_GForm_Responses_V1_100.csv'
GOOGLE_FORM_MTURK_RESPONSES_FILENAME_V1_ALL = '/Users/weichengcao/ActivityBuddyBundle/privadroid/exit_survey_analysis/PrivaDroid_Exit_Survey_MTurk_GForm_Responses_V1_total.csv'
GOOGLE_FORM_MTURK_RESPONSES_FILENAME_V2 = '/Users/weichengcao/ActivityBuddyBundle/privadroid/exit_survey_analysis/PrivaDroid_Exit_Survey_MTurk_GForm_Responses_V2.csv'
GOOGLE_FORM_MTURK_RESPONSES_FILENAME_V3 = '/Users/weichengcao/ActivityBuddyBundle/privadroid/exit_survey_analysis/PrivaDroid_Exit_Survey_MTurk_GForm_Responses_V3_100.csv'

class GoogleFormCronbachsAlpha:

    MTURK_ID_KEY = 'Your MTurk worker ID.'
    TIMESTAMP_KEY = 'Timestamp'

    # options are the same as the ones on Google Form
    LIKERT_SCALE_DICT = {}
    FIVE_LIKERT_SCALE_DICT = {
        'Strongly disagree': 0.0,
        'Disagree': 1/4,
        'Neither agree nor disagree': 1/2,
        'Agree': 3/4,
        'Strongly agree': 1.0
    }
    SEVEN_LIKERT_SCALE_DICT = {
        'Strongly disagree': 0.0,
        'Disagree': 1/6,
        'Somewhat disagree': 1/3,
        'Neither agree nor disagree': 1/2,
        'Somewhat agree': 2/3,
        'Agree': 5/6,
        'Strongly agree': 1.0
    }

    # questions are the same as the ones on Google Form
    INVERTED_QUESTIONS = {}
    V1_INVERTED_QUESTIONS = {
        "3. I don’t believe that mobile app privacy is compromised when the user loses control over their information as a result of an app usage.",
        "3. It is not very important to me that I am aware and knowledgeable about how my personal information will be used.",
        "4. I’m not concerned that smartphone apps are collecting too much personal information about me.",
        "3. Mobile app developers should be allowed to sell the personal information in their computer databases to other companies."
    }
    V2_INVERTED_QUESTIONS = {
        "3. I don't believe that mobile app privacy is compromised when the user loses control over their information as a result of an app usage.",
        "3. It is not very important to me that I am aware and knowledgeable about how my personal information will be used.",
        "4. I'm not concerned that smartphone apps are collecting too much personal information about me.",
        "3. Mobile app developers should be allowed to sell the personal information in their computer databases to other companies."
    }
    V3_INVERTED_QUESTIONS = {
        "4. I’m not concerned that smartphone apps are collecting too much personal information about me."
    }

    # questions are the same as the ones on Google Form
    QUESTION_SECTIONS = {}
    V1_QUESTION_SECTIONS = {
        'control': [
            "1. Mobile apps should allow users to exercise control and autonomy over decisions about how their information is collected, used, and shared.",
            "2. Consumer control of personal information is important to mobile app privacy. ",
            "3. I don’t believe that mobile app privacy is compromised when the user loses control over their information as a result of an app usage."
        ],
        'awareness': [
            "1. Mobile app developers seeking information should disclose the way the data are collected, processed, and used.",
            "2. A good mobile app privacy policy should have a clear and conspicuous disclosure.",
            "3. It is not very important to me that I am aware and knowledgeable about how my personal information will be used."
        ],
        'collection': [
            "1. It usually bothers me when smartphone apps ask me for personal information.",
            "2. When mobile apps ask me for personal information, I sometimes think twice before providing it.",
            "3. It bothers me to give personal information to so many mobile apps.",
            "4. I’m not concerned that smartphone apps are collecting too much personal information about me."
        ],
        'secondary use': [
            "1. Mobile apps should not use personal information for any purpose unless it has been authorized by the individuals who provided information.",
            "2. When people give personal information to a mobile app  for some reason, the app developer should never use the information for any other reason.",
            "3. Mobile app developers should be allowed to sell the personal information in their computer databases to other companies.",
            "4. Mobile app developers should never share personal information with other companies unless it has been authorized by the individual who provided the information."
        ]
    }
    V2_QUESTION_SECTIONS = {
        'control': [
            "1. Mobile app privacy is about a user's right to exercise control over decisions about how their information is collected, used, and shared.",
            "2. Consumer control of personal information is essential to mobile app privacy. ",
            "3. I don't believe that mobile app privacy is compromised when the user loses control over their information as a result of an app usage."
        ],
        'awareness': [
            "1. Mobile app developers seeking information should disclose the way the data are collected, processed, and used.",
            "2. A good mobile app privacy policy should have a clear and conspicuous disclosure. ",
            "3. It is not very important to me that I am aware and knowledgeable about how my personal information will be used."
        ],
        'collection': [
            "1. It usually bothers me when smartphone apps ask me for personal information. ",
            "2. When mobile apps ask me for personal information, I sometimes think twice before providing it.",
            "3. It bothers me to give personal information to so many mobile apps.",
            "4. I'm not concerned that smartphone apps are collecting too much personal information about me."
        ],
        'secondary use': [
            "1. Mobile apps should not use personal information for any purpose unless it has been authorized by the individuals who provided information.",
            "2. When people give personal information to a mobile app for some reason, the app developer should never use the information for any other reason.",
            "3. Mobile app developers should be allowed to sell the personal information in their computer databases to other companies.",
            "4. Mobile app developers should never share personal information with other companies unless it has been authorized by the individual who provided the information."
        ]
    }
    V3_QUESTION_SECTIONS = {
        'control': [
            "1. Mobile app privacy is about a user’s right to exercise control over decisions about how their information is collected, used, and shared.",
            "2. User control of personal information is essential to mobile app privacy. ",
            "3. I believe that mobile app privacy is compromised when the user loses control over their information as a result of app usage.",
            "4. I’m not concerned that smartphone apps are collecting too much personal information about me."
        ],
        'awareness': [
            "1. Mobile app developers seeking information should disclose the way the data are collected, processed, and used.",
            "2. A good mobile app privacy policy should have a clear and conspicuous disclosure.",
            "3. It is very important to me that I am aware and knowledgeable about how my personal information will be used."
        ],
        'collection': [
            "1. It usually bothers me when smartphone apps ask me for personal information.",
            "2. When mobile apps ask me for personal information, I sometimes think twice before providing it.",
            "3. It bothers me to give personal information to so many mobile apps.",
            "4. I’m concerned that smartphone apps are collecting too much personal information about me."
        ],
        'secondary use': [
            "1. Mobile apps should not use personal information for any purpose unless it has been authorized by the individuals who provided information.",
            "2. When people give personal information to a mobile app for some reason, the app developer should never use the information for any other reason.",
            "3. Mobile app developers should never sell the personal information in their computer databases to other companies.",
            "4. Mobile app developers should never share personal information with other companies unless it has been authorized by the individual who provided the information."
        ]
    }

    def __init__(self, data_csv_filename, version):
        self.data_csv_filename = data_csv_filename
        self.version = version
        if version == 1:
            self.LIKERT_SCALE_DICT = self.SEVEN_LIKERT_SCALE_DICT
            self.INVERTED_QUESTIONS = self.V1_INVERTED_QUESTIONS
            self.QUESTION_SECTIONS = self.V1_QUESTION_SECTIONS
        elif version == 2:
            self.LIKERT_SCALE_DICT = self.FIVE_LIKERT_SCALE_DICT
            self.INVERTED_QUESTIONS = self.V2_INVERTED_QUESTIONS
            self.QUESTION_SECTIONS = self.V2_QUESTION_SECTIONS
        elif version == 3:
            self.LIKERT_SCALE_DICT = self.FIVE_LIKERT_SCALE_DICT
            self.INVERTED_QUESTIONS = self.V3_INVERTED_QUESTIONS
            self.QUESTION_SECTIONS = self.V3_QUESTION_SECTIONS

        self.mturk_id_to_question_to_anwser_dict = dict()
        self.question_to_index_dict = dict()
        self.question_list = list()
        index_to_question_dict = dict()
        with open(data_csv_filename) as csv_file:
            csv_reader = csv.reader(csv_file, delimiter=',')
            line_count = 0
            for row in csv_reader:
                if line_count == 0:
                    for i in range(len(row)):
                        self.question_to_index_dict[row[i]] = i
                        index_to_question_dict[i] = row[i]
                        if row[i] != self.MTURK_ID_KEY and row[i] != self.TIMESTAMP_KEY:
                            self.question_list.append(row[i])
                else:
                    mturk_id = row[self.question_to_index_dict[self.MTURK_ID_KEY]]
                    self.mturk_id_to_question_to_anwser_dict[mturk_id] = dict()
                    for i in range(len(row)):
                        question = index_to_question_dict[i]
                        self.mturk_id_to_question_to_anwser_dict[mturk_id][question] = row[i]
                line_count += 1


    def get_valid_user_responses_dict(self):
        '''
        Filter out user responses that are not consistent.
        '''
        result = dict()

        # remove the user responses that answered the same for all questions
        for mturk_id in self.mturk_id_to_question_to_anwser_dict.keys():
            initial_answer = None
            has_different_answer = False
            for question in self.question_list:
                if initial_answer == None or self.mturk_id_to_question_to_anwser_dict[mturk_id][question] == initial_answer:
                    initial_answer = self.mturk_id_to_question_to_anwser_dict[mturk_id][question]
                else:
                    has_different_answer = True
                    break
            if has_different_answer:
                result[mturk_id] = self.mturk_id_to_question_to_anwser_dict[mturk_id]

        # 1. remove the user responses that answer 2 Agree/Strongly agree to inverted questions
        return result
        # final_result = dict()
        # for user_id in result.keys():
        #     wrong_answers_count = 0
        #     for question in self.INVERTED_QUESTIONS:
        #         if result[user_id][question] == 'Strongly agree' or result[user_id][question] == 'Agree':
        #             wrong_answers_count += 1
        #     if wrong_answers_count <= 4:
        #         final_result[user_id] = result[user_id]
        # return final_result


    def get_real_value_from_likert_scale_answer(self, answer, question):
        if question in self.INVERTED_QUESTIONS:
            return 1.0 - self.LIKERT_SCALE_DICT[answer]
        else:
            return self.LIKERT_SCALE_DICT[answer]


    def calculate_variance_between_answers(self):
        '''
        Create a matrix where rows are questions, columns are users
        '''
        answer_dict = dict()
        variance_dict = dict()
        valid_answers = self.get_valid_user_responses_dict()
        mturk_ids = list(valid_answers.keys())
        answer_values = dict()
        for section in self.QUESTION_SECTIONS.keys():
            answer_dict[section] = dict()
            variance_dict[section] = dict()
            stacked_values = list()
            for question in self.QUESTION_SECTIONS[section]:
                if self.version == 3 and question in self.INVERTED_QUESTIONS:
                    continue
                answer_dict[section][question] = np.zeros(len(valid_answers))
                for i in range(len(mturk_ids)):
                    answer_dict[section][question][i] = self.get_real_value_from_likert_scale_answer(valid_answers[mturk_ids[i]][question], question)
                variance_dict[section][question] = np.var(answer_dict[section][question])
                stacked_values.append(answer_dict[section][question])
            answer_values[section] = np.array(stacked_values)
        return variance_dict, answer_values


    def calculate_average_score_of_each_question(self):
        question_variance_dict, question_values_by_section_matrix = self.calculate_variance_between_answers()
        result = dict()
        for section in question_variance_dict.keys():
            result[section] = dict()
            questions = list(question_variance_dict[section].keys())
            for i in range(len(questions)):
                question = questions[i]
                result[section][question] = np.average(question_values_by_section_matrix[section][i])
        return result


    def calculate_covariance_between_questions(self):
        question_variance_dict, question_values_by_section_matrix = self.calculate_variance_between_answers()
        covariance_matrix_by_section = dict()
        for section in question_values_by_section_matrix.keys():
            covariance_matrix_by_section[section] = np.cov(question_values_by_section_matrix[section])
        return covariance_matrix_by_section


    def calculate_cronbachs_alpha(self):
        covariance_dict = self.calculate_covariance_between_questions()
        ca_dict = dict()
        for section in covariance_dict.keys():
            N = covariance_dict[section].diagonal().size
            average_v = np.average(covariance_dict[section].diagonal())
            average_c = (np.sum(covariance_dict[section]) - np.sum(covariance_dict[section].diagonal())) / (covariance_dict[section].size - N)
            ca_dict[section] = (N * average_c) / (average_v + (N - 1) * average_c)
        return ca_dict


    def pretty_print(self, json_obj):
        class NumpyEncoder(json.JSONEncoder):
            def default(self, obj):
                if isinstance(obj, np.ndarray):
                    return obj.tolist()
                return json.JSONEncoder.default(self, obj)
        json_dump = json.dumps(json_obj, cls=NumpyEncoder, indent=2)
        return json_dump