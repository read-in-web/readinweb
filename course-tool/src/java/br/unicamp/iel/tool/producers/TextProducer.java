package br.unicamp.iel.tool.producers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.org.ponder.rsf.builtin.UVBProducer;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import br.unicamp.iel.logic.ReadInWebCourseLogic;
import br.unicamp.iel.model.Activity;
import br.unicamp.iel.model.Answer;
import br.unicamp.iel.model.Course;
import br.unicamp.iel.model.DictionaryWord;
import br.unicamp.iel.model.FunctionalWord;
import br.unicamp.iel.model.Module;
import br.unicamp.iel.model.Question;
import br.unicamp.iel.tool.commons.CourseComponents;
import br.unicamp.iel.tool.viewparameters.CourseViewParameters;

public class TextProducer implements ViewComponentProducer, ViewParamsReporter {

    private static Log logger = LogFactory.getLog(TextProducer.class);
    @Setter
    private ReadInWebCourseLogic logic;

    public static final String VIEW_ID = "text";

    @Override
    public String getViewID() {
        return VIEW_ID;
    }

    @Override
    public void fillComponents(UIContainer tofill, ViewParameters viewparams,
            ComponentChecker checker) {

        CourseViewParameters parameters = (CourseViewParameters) viewparams;
        Course course = logic.getCourse(parameters.course);
        Activity activity;
        Module module;
        File audio;

        if(course == null){
            System.out.println("Course is null");
            return;
        } else {
            activity = logic.getActivity(parameters.activity);
            module = logic.getModule(parameters.module);
        }

        CourseComponents.loadMenu(parameters, tofill);
        CourseComponents.createModulesMenu(tofill, course, this.getViewID(),
                logic);
        CourseComponents.createBreadCrumb(tofill, module, activity);

        // Text Title
        UIOutput.make(tofill, "title_text", activity.getTitle());

        // Pre reading exercise
        if ((activity.getPrereading() != null)
                && (activity.getPrereading().compareToIgnoreCase("") != 0)) {
            UIVerbatim.make(tofill, "preLecture_title", "Pr\u00e9-leitura");
            UIVerbatim.make(tofill, "prelecture_text",
                    activity.getPrereading());
        }

        // Estimated time to read the text
        UIForm time_form = UIForm.make(tofill, "time_form");
        time_form.viewparams = new SimpleViewParameters(UVBProducer.VIEW_ID);

        UIInput time_time = UIInput.make(time_form, "time_text", null);
        if ((activity.getEtaRead() != null) && (activity.getEtaRead() > 0)) {
            time_time.setValue(Integer.toString(activity.getEtaRead() * 1000));
        } else {
            time_time.setValue("500");
        }

        UIInput activity_time = UIInput.make(time_form, "activity",
                "RegisterAccessAjaxBean.activity");
        activity_time.setValue(Long.toString(activity.getId()));

        UIInitBlock.make(tofill, "text_counter", "RIW.saveTextRead",
                new Object[] {activity_time, time_time,
                "RegisterAccessAjaxBean.results"});

        // Audio; fill it only if file exists
        // FIXME Fix up audio retrieve method
        audio = new File(this.makeSoundDir(activity));
        if (audio.exists()) {
            UIOutput.make(tofill, "sound_text",
                    this.makeSoundURL(activity));
        } else {
            UIOutput.make(tofill, "sound_text",
                    "Audio indispon\u00edvel.");
        }

        // Activity image
        if ((activity.getImage() != null)
                && (activity.getImage().compareToIgnoreCase("") != 0)) {
            // FIXME Fix up image retrieve
            UIOutput.make(tofill, "picture_text",
                    this.makeImageURL(activity));
        }

        // Fills the text of the activity
        UIVerbatim.make(tofill, "text_text", activity.getText());

        // Activity dictionary
        List<DictionaryWord> l_dw =
                new ArrayList<DictionaryWord>(logic.getDictionary(activity));
        String dictionary = "";
        for(DictionaryWord dw : l_dw){
            dictionary += ""
                    + "<li>"
                    + "<strong>" + dw.getWord() + ":</strong> "
                    + dw.getMeaning()
                    + "</li>";
        }
        UIVerbatim.make(tofill, "texto_dicionario", dictionary);

        // Functional Words
        List<FunctionalWord> l_fw = logic.getFunctionalWord(course);
        String functionalWords = "";
        for (FunctionalWord fw : l_fw) {
            functionalWords += ""
                    + "<li>"
                    + "<strong>" + fw.getWord() + ":</strong> "
                    + fw.getMeaning()
                    + "</li>";
        }
        UIVerbatim.make(tofill, "texto_palavrasfuncao", functionalWords);

        // Question and Answers
        List<Question> l_q =
                new ArrayList<Question>(logic.getQuestions(activity));
        for(Question q : l_q){
            UIBranchContainer row = UIBranchContainer.make(tofill, "li-rows:",
                    Integer.toString(q.getPosition()));
            UILink ui_l = UILink.make(row, "input_link_q_",
                    Integer.toString(q.getPosition()),
                    "/#question_" + q.getId());
            ui_l.updateFullID("input_link_q_" + q.getId());

            UIBranchContainer ui_bc = UIBranchContainer.make(tofill,
                    "div_questions:");
            ui_bc.updateFullID("question_" + q.getId());

            UIVerbatim.make(ui_bc, "question", q.getQuestion());
            UIVerbatim.make(ui_bc, "suggested_answer", q.getSuggestedAnswer());

            UIForm answer_form = UIForm.make(ui_bc, "answer_form");
            answer_form.viewparams =
                    new SimpleViewParameters(UVBProducer.VIEW_ID);

            UIInput ui_question = UIInput.make(answer_form, "question_id",
                    "AnswerAjaxBean.question");
            ui_question.updateFullID("question_to_" + q.getId());
            ui_question.setValue(Long.toString(q.getId()));

            Answer a = logic.getAnswerByQuestionAndUser(q.getId());
            UIInput ui_answer = UIInput.make(answer_form, "answer",
                    "AnswerAjaxBean.answer");
            ui_answer.updateFullID("answer_to_" + q.getId());
            ui_answer.setValue(a != null ? a.getAnswer() : "");

            UICommand ui_send = UICommand.make(answer_form, "send_answer");
            ui_send.updateFullID("send_to_" + q.getId());

            UIInitBlock.make(tofill, "init_js:", "RIW.saveUserAnswer",
                    new Object[] {ui_answer, ui_question, ui_send,
                    "AnswerAjaxBean.results"});
        }

        logic.registerAccess("Acesso ao texto.", this.getViewID(), activity);
    }

    @Override
    public ViewParameters getViewParameters() {
        return (new CourseViewParameters(this.getViewID()));
    }

    /**
     *
     * @param activity
     * @return
     */
    private String makeImageURL(Activity activity) {
        String str_retorno, str_imagem;
        Long int_numAct;

        str_imagem = activity.getImage();
        int_numAct = activity.getId();

        str_retorno = "<img src='/readinweb-tool/content/modulos/modulo"
                + "/atividades/atividade"
                + makeTwoCharNumber(int_numAct)
                + "/texto/imagens/" + str_imagem + "' id='textactimage' />"
                + "<div align=\"center\"><div class=\"switch\" id=\"buton_image\" "
                + "align=\"center\">IMAGEM</div></div>";

        return (str_retorno);
    }

    /**
     * Method very 'porreta' which puts a 'zero' before the numbers that dont have 2 chars
     * @param int_numberArg
     * @return
     */
    public static String makeTwoCharNumber(Long int_numberArg){
        if ((int_numberArg < 10) && (int_numberArg >= 0)){
            return ("0" + int_numberArg);
        } else {
            return (Long.toString(int_numberArg));
        }
    }

    /**
     *
     * @param activity
     * @return
     */
    private String makeSoundURL(Activity activityArg) {
        String str_retorno;

        str_retorno = "<script type='text/javascript'>"
                + "var so = new SWFObject('../../../readinweb-tool/content/audio/playerSingle.swf', 'mymovie', '192', '67', '7', '#FFFFFF');"
                + "so.addVariable('autoPlay', 'no');"
                + "so.addVariable('soundPath', '../../../readinweb-tool/content/audio/" + this.getAudioFileName(activityArg) + "');"
                + "so.write('audio_player');"
                + "so.addVariable('playerSkin','1');"
                + "</script>";

        return (str_retorno);
    }

    /**
     *
     * @param activity
     * @return
     */
    private String makeSoundDir(Activity activityArg) {
        String str_retorno;
        str_retorno = makeContentDir() + "audio/" + this.getAudioFileName(activityArg);

        return (str_retorno);
    }

    /**
     *
     * @return
     */
    private static String makeContentDir() {
        String str_userDir, str_retorno;

        str_userDir = System.getProperty("user.dir");
        if (!str_userDir.endsWith("/bin")) {
            // se for no Linux (local)
            str_retorno = str_userDir + "/tool/src/webapp/content/";
        } else {
            // se for no Unix (servidor)
            str_retorno = str_userDir.substring(0, System.getProperty("user.dir").length() - 4) + "/webapps/readinweb-tool/content/";
        }
        return (str_retorno);
    }

    /**
     * The audio file name has a standard format this method, given the
     * activity, returns the correct name of file
     *
     * @param activity
     * @return
     */
    private String getAudioFileName(Activity activity) {
        String str_retorno;
        long int_numAct, int_numMod;

        // try to get the module and the activity Id
        if ((activity == null)) {
            if (true)
//            (session != null) {
//                int_numAct = session.getActivity();
//                int_numMod = session.getModule();
//            } else
            {
                int_numAct = 0;
                int_numMod = 0;
            }
        } else {
            int_numAct = activity.getId();
            int_numMod = activity.getModule().getId();
        }

        str_retorno = "m" + int_numMod + "a" + int_numAct + ".mp3";
        return (str_retorno);
    }
}