package cop5556sp17.AST;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;


public abstract class Chain extends Statement {
	private Kind arrow=Kind.ARROW;
	private TypeName t;
	
	public Chain(Token firstToken) {
		super(firstToken);
	}
	public TypeName getT() {
		return t;
	}
	public void setT(TypeName t) {
		this.t = t;
	}
	public Kind getA() {
		return arrow;
	}
	public void setA(Kind arrow) {
		this.arrow = arrow;
	}

}
