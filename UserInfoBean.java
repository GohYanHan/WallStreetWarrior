public class UserInfoBean {
    private String userid;
    private String password;
    private String name;
    private String sex;
    private String birthday;
    private String email;
    private String telephone;
    // �˻���Ϣ��Ŀǰһ��һ���˻���
    private String accountBalance;
    private String lastModify;

    public UserInfoBean() {
        super();
    }

    /**
     * @param userid
     * @param password
     * @param name
     * @param sex
     * @param birthday
     * @param email
     * @param telephone
     * @param accountBalance
     * @param lastModify
     */
    public UserInfoBean(String userid, String password, String name,
                        String sex, String birthday, String email, String telephone,
                        String accountBalance, String lastModify) {
        super();
        this.userid = userid;
        this.password = password;
        this.name = name;
        this.sex = sex;
        this.birthday = birthday;
        this.email = email;
        this.telephone = telephone;
        this.accountBalance = accountBalance;
        this.lastModify = lastModify;
    }

    /**
     * @return userid
     */
    public String getUserid() {
        return userid;
    }

    /**
     * @param userid
     *           userid
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *          password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *           name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return sex
     */
    public String getSex() {
        return sex;
    }

    /**
     * @param sex
     *            sex
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * @return birthday
     */
    public String getBirthday() {
        return birthday;
    }

    /**
     * @param birthday
     *           birthday
     */
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    /**
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     *           email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return telephone
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * @param telephone
     *          telephone
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * @return accountBalance
     */
    public String getAccountBalance() {
        return accountBalance;
    }

    /**
     * @param accountBalance
     *          accountBalance
     */
    public void setAccountBalance(String accountBalance) {
        this.accountBalance = accountBalance;
    }

    /**
     * @return lastModify
     */
    public String getLastModify() {
        return lastModify;
    }

    /**
     * @param lastModify
     *          lastModify
     */
    public void setLastModify(String lastModify) {
        this.lastModify = lastModify;
    }
}