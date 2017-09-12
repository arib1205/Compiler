package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;

public class IdentChain extends ChainElem {

	private Dec d;
	
	public IdentChain(Token firstToken) {
		super(firstToken);
	}


	@Override
	public String toString() {
		return "IdentChain [firstToken=" + firstToken + "]";
	}


	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentChain(this, arg);
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
