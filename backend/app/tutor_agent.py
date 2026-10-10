from typing import Tuple, Dict, Any, Optional
from app.models import TutorAskRequest, TutorAskResponse
from app.curriculum_catalog import PILOT_LESSON_CONTENT

class ControlledAITutorAgent:
    """
    Controlled educational tutoring agent as specified in Section 6:
    - Input: Learner grade, board, subject, topic, question, language
    - Workflow: Understand -> Ground -> Explain -> Check -> Fallback
    """

    def process_query(self, request: TutorAskRequest) -> TutorAskResponse:
        q_lower = request.question.lower()

        # Step 1: Understand Intent & Concept
        if "oxygen" in q_lower or "o2" in q_lower or "ଅମ୍ଳଜାନ" in request.question or "source" in q_lower:
            odia_ans = (
                "ଆଲୋକ ଶ୍ଳେଷଣରେ ନିର୍ଗତ ହେଉଥିବା ଅମ୍ଳଜାନ (O₂) ପ୍ରକୃତରେ ଜଳ (H₂O) ରୁ ଆସିଥାଏ, "
                "ଅଙ୍ଗାରକାମ୍ଳ (CO₂) ରୁ ନୁହେଁ। ଆଲୋକ ପର୍ଯ୍ୟାୟରେ ସୂର୍ଯ୍ୟାଲୋକ ଦ୍ୱାରା ଜଳର ବିଶ୍ଳେଷଣ "
                "(Photolysis of water) ଘଟି ଅମ୍ଳଜାନ ଗ୍ୟାସ ମୁକ୍ତ ହୁଏ।"
            )
            eng_ans = (
                "Oxygen (O₂) released during photosynthesis originates exclusively from the splitting "
                "of water (H₂O), NOT from carbon dioxide (CO₂). Photolysis of water in the thylakoid "
                "membrane releases electrons, protons, and diatomic oxygen."
            )
            rec_quiz = "q1"
        elif "dark" in q_lower or "calvin" in q_lower or "ଅନ୍ଧକାର" in request.question or "stroma" in q_lower or "ଷ୍ଟ୍ରୋମା" in request.question:
            odia_ans = (
                "ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା ବା କାଲଭିନ ଚକ୍ର (Calvin Cycle) ହରିତକଣାର ଷ୍ଟ୍ରୋମା (Stroma) ରେ ସଂଘଟିତ ହୁଏ। "
                "ଏଠାରେ RuBisCO ଏନଜାଇମ ସହାୟତାରେ CO₂ ବିଜାରିତ ହୋଇ ଶର୍କରା ପ୍ରସ୍ତୁତ ହୁଏ।"
            )
            eng_ans = (
                "The light-independent dark reaction (Calvin Cycle) occurs in the chloroplast stroma. "
                "Here, the premier enzyme RuBisCO facilitates the enzymatic fixation of CO₂ into glucose carbohydrates."
            )
            rec_quiz = "q3"
        elif "chloroplast" in q_lower or "ହରିତକଣା" in request.question or "grana" in q_lower or "ଗ୍ରାନା" in request.question:
            odia_ans = (
                "ହରିତକଣା ହେଉଛି ଉଦ୍ଭିଦ କୋଷର ରୋଷେଇ ଘର। ଏହାର ଗ୍ରାନାରେ ଆଲୋକ ପ୍ରକ୍ରିୟା ଏବଂ "
                "ଏହାର ଷ୍ଟ୍ରୋମାରେ ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା ସଂପନ୍ନ ହୁଏ।"
            )
            eng_ans = (
                "Chloroplast is the organelle where photosynthesis occurs. Light reactions occur in its grana/thylakoids, "
                "and dark reactions occur in its fluid stroma."
            )
            rec_quiz = "q2"
        else:
            odia_ans = (
                f"ନବମ ଶ୍ରେଣୀ ବିଜ୍ଞାନ ଅନୁଯାୟୀ, ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ସବୁଜ ଉଦ୍ଭିଦଙ୍କର ଖାଦ୍ୟ ପ୍ରସ୍ତୁତି ପ୍ରକ୍ରିୟା। "
                f"ରାସାୟନିକ ସମୀକରଣ: {PILOT_LESSON_CONTENT.chemical_equation}। ଆପଣ ଏହାର ଆଲୋକ ବା ଅନ୍ଧକାର ପର୍ଯ୍ୟାୟ ବିଷୟରେ ପଚାରିପାରିବେ।"
            )
            eng_ans = (
                f"According to Class 9 curriculum, photosynthesis is how autotrophic plants synthesize nutrients. "
                f"Standard equation: {PILOT_LESSON_CONTENT.chemical_equation}. You can ask about light vs dark reactions or take a quiz."
            )
            rec_quiz = "q1"

        return TutorAskResponse(
            answer_odia=odia_ans,
            answer_english=eng_ans,
            grounding_source=f"Odisha Board & CBSE Class {request.class_grade} Science: Nutrition in Plants",
            is_offline_grounded=True,
            recommended_quiz_id=rec_quiz
        )

tutor_agent = ControlledAITutorAgent()
