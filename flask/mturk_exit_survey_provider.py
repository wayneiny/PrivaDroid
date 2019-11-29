from exit_survey_analysis.GoogleFormCronbachsAlpha import GoogleFormCronbachsAlpha

def calculate_cronbachs_alpha(filename, version):
    calculator = GoogleFormCronbachsAlpha(filename, version)
    result = dict()
    result['cronbachs_alpha'] = calculator.calculate_cronbachs_alpha()
    result['covariance_matrix'] = calculator.pretty_print(calculator.calculate_covariance_between_questions())
    result['average_by_question (values are inverted for inverted questions)'] = calculator.calculate_average_score_of_each_question()
    return result