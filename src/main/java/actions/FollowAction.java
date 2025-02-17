package actions;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeConverter;
import actions.views.EmployeeView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import models.Employee;
import models.Follow;
import services.EmployeeService;
import services.FollowService;
import services.ReportService;

public class FollowAction extends ActionBase {

    private FollowService folService;
    private ReportService repService;
    private EmployeeService empService;

    @Override
    public void process() throws ServletException, IOException {

        folService = new FollowService();
        repService = new ReportService();
        empService = new EmployeeService();

        invoke();

        folService.close();
        repService.close();
        empService.close();

    }

    /**
     * 一覧を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void index() throws ServletException, IOException {

        int page = getPage();

        //ログイン従業員を取得
        Employee loginEmp = EmployeeConverter.toModel(getSessionScope(AttributeConst.LOGIN_EMP));

        //フォロー中の従業員件数取得
        long count = empService.getCountFolloweeByLoginId(loginEmp);

        //フォロー中の従業員一覧取得
        List<EmployeeView> followedEmp = empService.getEmpByLoginIdPerPage(loginEmp, page);

        putRequestScope(AttributeConst.PAGE, page);
        putRequestScope(AttributeConst.EMP_COUNT, count);
        putRequestScope(AttributeConst.EMPLOYEES, followedEmp);
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE);
        forward(ForwardConst.FW_FOL_INDEX);
    }


    /**
     * フォローする
     * @throws ServletException
     * @throws IOException
     */
    public void create() throws ServletException, IOException {

        //ログイン従業員のデータを取得（EmployeeView型からEmployee型にキャスト）
        Employee loginEmp = EmployeeConverter.toModel(getSessionScope(AttributeConst.LOGIN_EMP));

        //フォロー対象従業員のデータを取得（EmployeeView型からEmployee型にキャスト）
        Employee followedEmp = EmployeeConverter.toModel(empService.findOne(toNumber(getRequestParam(AttributeConst.EMP_ID))));

        //Followモデルにデータをセット
        Follow fol = new Follow();
        LocalDateTime ldt = LocalDateTime.now();
        fol.setEmployee(loginEmp);
        fol.setFollowedEmployee(followedEmp);
        fol.setCreatedAt(ldt);
        fol.setUpdatedAt(ldt);

        List<String> errors = folService.create(loginEmp, followedEmp, fol);

        if (errors.size() > 0) {
            putSessionScope(AttributeConst.ERR, errors);
            redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);

        } else {
            putSessionScope(AttributeConst.FLUSH, MessageConst.I_FOLLOWED.getMessage());
            redirect(ForwardConst.ACT_REP, ForwardConst.CMD_SHOW);
        }
    }


    /**
     * アンフォローする
     * @throws ServletException
     * @throws IOException
     */
    public void destroy() throws ServletException, IOException {

        //ログイン従業員のデータを取得（EmployeeView型からEmployee型にキャスト）
        Employee loginEmp = EmployeeConverter.toModel(getSessionScope(AttributeConst.LOGIN_EMP));

        //フォロー対象従業員のデータを取得（EmployeeView型からEmployee型にキャスト）
        Employee followedEmp = EmployeeConverter.toModel(empService.findOne(toNumber(getRequestParam(AttributeConst.EMP_ID))));

        folService.destroy(loginEmp, followedEmp);

        putSessionScope(AttributeConst.FLUSH, MessageConst.I_UNFOLLOWED.getMessage());
        redirect(ForwardConst.ACT_REP, ForwardConst.CMD_SHOW);
    }


}