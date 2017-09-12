package cop5556sp17;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import cop5556sp17.AST.Dec;


public class SymbolTable {
	
	class Node{
		int currScope;
		Dec decl;
		
		Node(int c, Dec d){
			currScope = c;
			decl = d;
		}
	}
	
	int currentScope,nextScope=1;
	Stack<Integer> scope_stack;
	Map<String,ArrayList<Node>> symbol;
	
	//TODO  add fields

	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		currentScope=nextScope++;
		scope_stack.push(currentScope);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS

		if(!scope_stack.isEmpty())
			scope_stack.pop();
		if(!scope_stack.isEmpty())
			currentScope = scope_stack.peek();
	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		
		Node temp = new Node(currentScope, dec);
		
		if(symbol.containsKey(ident))
		{
			ArrayList<Node> list = symbol.get(ident);
			list.add(0, temp);
		}
		else
		{
			ArrayList<Node> list = new ArrayList<>();
			list.add(temp);
			symbol.put(ident, list);
		}
			
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		if(symbol.containsKey(ident))
		{
			ArrayList<Node> list = symbol.get(ident);
			
			for(int i=0;i<list.size();i++)
			{
				int chk = list.get(i).currScope;
				if(scope_stack.search(chk)!=-1)
					return list.get(i).decl;
			}
			
			return null;
		}
		else
			return null;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		scope_stack = new Stack<>();
		scope_stack.push(0);
		symbol = new HashMap<>();
	}

	public boolean presentInScope(String ident)
	{
		if(symbol.containsKey(ident))
			{
			ArrayList<Node> list = symbol.get(ident);
			for(int i=0;i<list.size();i++)
				{
				if(list.get(i).currScope == currentScope)
					return true;
				}
			return false;
			}
		else
			return false;
	}

	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}
	
	


}
