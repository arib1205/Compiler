package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	
	Stack<Integer> sn=new Stack<Integer>();
	int sc=1;
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);

		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		// ArrayList<ParamDec> params = program.getParams();
		int i = 0;
		for (ParamDec dec : program.getParams()) {
			dec.setSlotNum(i++);
			cw.visitField(0,dec.getIdent().getText(),dec.getT().getJVMTypeDesc(),null,null);
			dec.visit(this, mv);
		}
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		// TODO visit the local variables
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method

		cw.visitEnd();// end of class

		// generate classfile and return it
		return cw.toByteArray();
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, null);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getT());
		assignStatement.getVar().visit(this, null);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		
		Chain chain=binaryChain.getE0();
		ChainElem chainelem=binaryChain.getE1();
		
		if(binaryChain.getArrow().kind==BARARROW)
			chainelem.setA(BARARROW);
		
		chain.visit(this, false);
		
		TypeName typchain=chain.getT();
		TypeName typelem=chainelem.getT();
		
		if(typchain==TypeName.FILE)
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile",PLPRuntimeImageIO.readFromFileDesc, false);
		else if(typchain==TypeName.URL) 
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL",PLPRuntimeImageIO.readFromURLSig, false);
		
		chainelem.visit(this, true);
		if(chainelem instanceof IdentChain) 
			{
			IdentChain i = (IdentChain) chainelem;
			
			if(typelem.isType(TypeName.INTEGER,TypeName.BOOLEAN))
				mv.visitVarInsn(ILOAD, i.getD().getSNumber());
			else if(i.getD() instanceof ParamDec) 
				{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD,className,i.getD().getIdent().getText(),i.getD().getT().getJVMTypeDesc());
				} 
			else
				mv.visitVarInsn(ALOAD, i.getD().getSNumber());
			}
		
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		
		TypeName e0=binaryExpression.getE0().getT();
		TypeName e1=binaryExpression.getE1().getT();
		Token op=binaryExpression.getOp();
		
		if(op.isKind(AND,OR))
			{
			binaryExpression.getE0().visit(this, null);
			Label bo=new Label();
			Label b1=new Label();
			
			if(op.kind==AND)
				{
				mv.visitJumpInsn(IFEQ, bo);
				binaryExpression.getE1().visit(this, null);
				mv.visitJumpInsn(IFEQ, bo);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, b1);
				mv.visitLabel(bo);
				mv.visitInsn(ICONST_0);
				}
			
			else if(op.kind==OR)
				{
				mv.visitJumpInsn(IFNE, bo);
				binaryExpression.getE1().visit(this, null);
				mv.visitJumpInsn(IFNE, bo);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, b1);
				mv.visitLabel(bo);
				mv.visitInsn(ICONST_1);
				}
			
			mv.visitLabel(b1);
			
			}
		else
			{
			binaryExpression.getE0().visit(this, null);
			binaryExpression.getE1().visit(this, null);
			
			if(op.kind==PLUS)
				{
				if(e0==TypeName.INTEGER)
					mv.visitInsn(IADD);
				else 
					mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageOps.JVMName,"add",PLPRuntimeImageOps.addSig,false);
				}
			
			else if(op.kind==MINUS)
				{
				if(e0==TypeName.INTEGER)
					mv.visitInsn(ISUB);
				else 
					mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageOps.JVMName,"sub",PLPRuntimeImageOps.subSig,false);
				}
				
			else if(op.kind==TIMES)
				{
				if(e0==TypeName.INTEGER && e1==TypeName.INTEGER) 
					mv.visitInsn(IMUL);
				else if(e0==TypeName.INTEGER && e1==TypeName.IMAGE) 
					{
					mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageOps.JVMName,"mul",PLPRuntimeImageOps.mulSig,false);
					} 
				else 
					mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageOps.JVMName,"mul",PLPRuntimeImageOps.mulSig,false);
				}

			else if(op.kind==DIV)
				{
				if(e0==TypeName.INTEGER && e1==TypeName.INTEGER)
					mv.visitInsn(IDIV);
				else
					mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageOps.JVMName,"div", PLPRuntimeImageOps.divSig,false);
				}

			else if(op.kind==MOD)
				{
				if(e0==TypeName.INTEGER && e1==TypeName.INTEGER)
					mv.visitInsn(IREM);
				else
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
				}
						
			else if(op.isKind(EQUAL,NOTEQUAL,LT,GT,LE,GE))
				{
				Label ass0=new Label();
				Label ass1=new Label();
				
				if(op.kind==EQUAL)
					mv.visitJumpInsn(IF_ICMPNE, ass0);
					
				else if(op.kind==NOTEQUAL)
					mv.visitJumpInsn(IF_ICMPEQ, ass0);
									
				else if(op.kind==LT)
					mv.visitJumpInsn(IF_ICMPGE, ass0);
						
				else if(op.kind==GT)
					mv.visitJumpInsn(IF_ICMPLE, ass0);
							
				else if(op.kind==LE)
					mv.visitJumpInsn(IF_ICMPGT, ass0);
				
				else if(op.kind==GE)
					mv.visitJumpInsn(IF_ICMPLT, ass0);
							
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, ass1);
				mv.visitLabel(ass0);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(ass1);
				}
			}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		
		Label l0=new Label();
		Label l1 = new Label();
		mv.visitLineNumber(block.getFirstToken().getLinePos().line,l0);
		mv.visitLabel(l0);
		mv.visitLineNumber(0, l1);
		mv.visitLabel(l1);
		
		List<Dec> decs = block.getDecs();
		List<Statement> stat = block.getStatements();
		
		for(int i=0; i<decs.size(); i++)
			{
			decs.get(i).visit(this, null);
			}
		for(int i=0; i<stat.size(); i++)
			{
			stat.get(i).visit(this, null);
			}
		
		for(int i=0; i<decs.size(); i++,sc=sc-1,sn.pop())
			{
			decs.get(i).visit(this, null);
			mv.visitLocalVariable(decs.get(i).getIdent().getText(),decs.get(i).getT().getJVMTypeDesc(),null,l0,l1,decs.get(i).getSNumber());
			}
		
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		
		if(booleanLitExpression.getValue()==true)
			mv.visitInsn(ICONST_1);
		else
			mv.visitInsn(ICONST_0);

		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		
		Token t=constantExpression.getFirstToken();
		
		if(t.kind==KW_SCREENWIDTH)	
			mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeFrame.JVMClassName,"getScreenWidth",PLPRuntimeFrame.getScreenWidthSig,false);
		else if(t.kind==KW_SCREENHEIGHT)
			mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeFrame.JVMClassName,"getScreenHeight",PLPRuntimeFrame.getScreenHeightSig,false);
		
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		
		sn.push(sc);
		sc++;
		declaration.setSNumber(sn.peek());
		TypeName t=declaration.getT();
		
		if(t==FRAME||t==IMAGE)
			{
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getSNumber());
			}
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		
		Token t=filterOpChain.getFirstToken();
		String string="";
		
		if(t.isKind(OP_GRAY,OP_BLUR,OP_CONVOLVE))
			{
			if(t.kind==OP_GRAY)
				{
				string="grayOp";
				
				if(filterOpChain.getA()==BARARROW)
					{
					mv.visitInsn(DUP);
					mv.visitInsn(SWAP);
					}
				else
					mv.visitInsn(ACONST_NULL);
				}
			
			else if(t.kind==OP_BLUR)
				{
				string="blurOp";
				mv.visitInsn(ACONST_NULL);
				}
				
			else if(t.kind==OP_CONVOLVE)
				{
				string="convolveOp";
				mv.visitInsn(ACONST_NULL);
				}
			
			mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeFilterOps.JVMName,string,PLPRuntimeFilterOps.opSig,false);		
			}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		
		frameOpChain.getArg().visit(this, null);
		Token t=frameOpChain.getFirstToken();
		
		if(t.kind==KW_MOVE)
			mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"moveFrame",PLPRuntimeFrame.moveFrameDesc,false);
		
		else if(t.kind==KW_SHOW)
			mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"showImage",PLPRuntimeFrame.showImageDesc,false);
				
		else if(t.kind==KW_HIDE)
			mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"hideImage",PLPRuntimeFrame.hideImageDesc,false);
			
		else if(t.kind==KW_XLOC)
			mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"getXVal",PLPRuntimeFrame.getXValDesc,false);
	
		else if(t.kind==KW_YLOC)
			mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"getYVal",PLPRuntimeFrame.getYValDesc,false);
	
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		
		Dec idChain=identChain.getD();
		TypeName t=idChain.getT();
		String string=idChain.getIdent().getText();
		
		if((boolean)arg==true) 
			{
			if(idChain instanceof ParamDec && t.isType(TypeName.INTEGER,TypeName.FILE)) 
				{
				mv.visitVarInsn(ALOAD,0);
				if(t==TypeName.INTEGER)
					{
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD,className,string,t.getJVMTypeDesc());
					}
				
				else if(t==TypeName.FILE)
					{
					mv.visitFieldInsn(GETFIELD,className,string,t.getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageIO.className,"write",PLPRuntimeImageIO.writeImageDesc, false);
					}
				idChain.setBool(true);
				} 
			
			else 
				{
				if(t.isType(TypeName.INTEGER,TypeName.IMAGE,TypeName.FILE))
					{
					if(t==TypeName.INTEGER)
						{
						mv.visitVarInsn(ISTORE,idChain.getSNumber());
						}
					
					else if(t==TypeName.IMAGE)
						{
						mv.visitVarInsn(ASTORE,idChain.getSNumber());
						}
					
					else if(t==TypeName.FILE)
						{
						mv.visitVarInsn(ALOAD, idChain.getSNumber());
						mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageIO.className,"write",PLPRuntimeImageIO.writeImageDesc,false);
						}
					idChain.setBool(true);
					}
				
				else if(t==TypeName.FRAME)
					{
					Boolean b=idChain.getBool();
					if(b==true)
						mv.visitVarInsn(ALOAD,idChain.getSNumber());
					else
						mv.visitInsn(ACONST_NULL);
											
					mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeFrame.JVMClassName,"createOrSetFrame",PLPRuntimeFrame.createOrSetFrameSig, false);
					mv.visitInsn(DUP);
					mv.visitVarInsn(ASTORE,idChain.getSNumber());
					if(b==false)
						idChain.setBool(true);
					}
				}
			} 
		else 
			{
			if(idChain instanceof ParamDec)
				{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD,className,string,t.getJVMTypeDesc());
				}
			else
				{
				TypeName typ=identChain.getT();
				if(typ.isType(TypeName.FRAME,TypeName.IMAGE))
					{
					Boolean bool=idChain.getBool();
					if(bool==true)
						mv.visitVarInsn(ALOAD, idChain.getSNumber());
					else
						mv.visitInsn(ACONST_NULL);
					} 
				else if(typ.isType(TypeName.INTEGER,TypeName.BOOLEAN)) 
					mv.visitVarInsn(ILOAD, idChain.getSNumber());
					
				}

		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		
		Dec dec=identExpression.getD();
		
		if(dec instanceof ParamDec)
			{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD,className,dec.getIdent().getText(),dec.getT().getJVMTypeDesc());
			}
		else 
			{
			TypeName t=identExpression.getT();
			if(t.isType(TypeName.INTEGER,TypeName.BOOLEAN))
				mv.visitVarInsn(ILOAD, dec.getSNumber());
			else 
				mv.visitVarInsn(ALOAD, dec.getSNumber());
			}

		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		
		Dec dec=identX.getD();
		TypeName t=dec.getT();
		
		if(dec instanceof ParamDec)
			{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD,className,dec.getIdent().getText(),t.getJVMTypeDesc());
			}
		else 
			{
			if(dec.getT()==TypeName.IMAGE)
				{
				mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageOps.JVMName,"copyImage",PLPRuntimeImageOps.copyImageSig,false);
				mv.visitVarInsn(ASTORE,dec.getSNumber());
				}
			else if(t==TypeName.FRAME)
				mv.visitVarInsn(ASTORE,dec.getSNumber());
			else 
				mv.visitVarInsn(ISTORE,dec.getSNumber());

			dec.setBool(true);
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		
		Label if0=new Label();
		Label if1=new Label();
		ifStatement.getE().visit(this, null);
		mv.visitJumpInsn(IFEQ,if0);
		mv.visitLabel(if1);
		ifStatement.getB().visit(this, null);
		mv.visitLabel(if0);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		
		imageOpChain.getArg().visit(this, null);
		Token t=imageOpChain.getFirstToken();
		if(t.kind==OP_HEIGHT)
			mv.visitMethodInsn(INVOKEVIRTUAL,"java/awt/image/BufferedImage","getHeight","()I",false);
		
		else if(t.kind==OP_WIDTH)
			mv.visitMethodInsn(INVOKEVIRTUAL,"java/awt/image/BufferedImage","getWidth","()I",false);

		else if(t.kind==KW_SCALE)
			mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageOps.JVMName,"scale",PLPRuntimeImageOps.scaleSig,false);

		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		
		MethodVisitor mv = (MethodVisitor) arg;
		TypeName t = paramDec.getT();
		String string=paramDec.getIdent().getText();
		
		if(t.isType(TypeName.INTEGER,TypeName.BOOLEAN,TypeName.URL))
			{
			mv.visitVarInsn(ALOAD,0);
			mv.visitVarInsn(ALOAD,1);
			mv.visitLdcInsn(paramDec.getSlotNum());
			
			if(t==TypeName.INTEGER)
				{
				mv.visitInsn(AALOAD);
				mv.visitMethodInsn(INVOKESTATIC,"java/lang/Integer","parseInt","(Ljava/lang/String;)I",false);
				mv.visitFieldInsn(PUTFIELD,className,string,"I");
				}
			
			else if(t==TypeName.BOOLEAN)
				{
				mv.visitInsn(AALOAD);
				mv.visitMethodInsn(INVOKESTATIC,"java/lang/Boolean","parseBoolean","(Ljava/lang/String;)Z",false);
				mv.visitFieldInsn(PUTFIELD,className,string,"Z");
				}
			
			else if(t==TypeName.URL)
				{
				mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageIO.className,"getURL",PLPRuntimeImageIO.getURLSig,false);
				mv.visitFieldInsn(PUTFIELD,className,string,"Ljava/net/URL;");
				}
			}
		
		else if(t==TypeName.FILE)
			{
			mv.visitVarInsn(ALOAD,0);
			mv.visitTypeInsn(NEW,"java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD,1);
			mv.visitLdcInsn(paramDec.getSlotNum());
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL,"java/io/File","<init>","(Ljava/lang/String;)V",false);
			mv.visitFieldInsn(PUTFIELD,className,string,"Ljava/io/File;");
			}
	
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		
		sleepStatement.getE().visit(this, null);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		
		List<Expression> list = tuple.getExprList();
		
		for(int i=0;i<list.size();i++)
			{
			list.get(i).visit(this, null);
			}
		
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		
		Label wh0 = new Label();
		Label wh1 = new Label();
		mv.visitJumpInsn(GOTO, wh0);
		mv.visitLabel(wh1);
		whileStatement.getB().visit(this, null);
		mv.visitLabel(wh0);
		whileStatement.getE().visit(this, null);
		mv.visitJumpInsn(IFNE, wh1);
		return null;
	}

}
