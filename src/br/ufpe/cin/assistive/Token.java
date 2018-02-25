package br.ufpe.cin.assistive;

public class Token {
    private int typeId, propletId;
    private String propletName;

    Token(int typeId, int propletId, String propletName) {
        this.typeId = typeId;
        this.propletId = propletId;
        this.propletName = propletName;
    }

    public void setTypeId(int typeId) { this.typeId = typeId; }
    public int getTypeId() { return this.typeId; }

    public void setPropletId(int propletIdId) { this.propletId = propletIdId; }
    public int getPropletId() { return this.propletId; }

    public void setPropletName(String propletName) { this.propletName = propletName; }
    public String getPropletName() { return this.propletName; }
}
