package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;

public class IdentLValue extends ASTNode {
	
	private Dec d;
	
	public IdentLValue(Token firstToken) {
		super(firstToken);
	}
	
	@Override
	public String toString() {
		return "IdentLValue [firstToken=" + firstToken + "]";
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentLValue(this,arg);
	}

	public String getText() {
		return firstToken.getText();
	}

	public Dec getD() {
		return d;
	}

	public void setD(Dec d) {
		this.d = d;
	}

}
