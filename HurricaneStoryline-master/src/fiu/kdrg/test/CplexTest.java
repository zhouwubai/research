package fiu.kdrg.test;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class CplexTest {
	
	public static void main(String[] args) {
		
		try {
			IloCplex cplex = new IloCplex();
			
			double[] lb = {0.0,0.0,0.0};
			double[] ub = {40.0, Double.MAX_VALUE, Double.MAX_VALUE};
			IloNumVar[] x = cplex.numVarArray(3, lb, ub);
			
			
			double[] objvals = {1.0,2.0,3.0};
			cplex.addMaximize(cplex.scalProd(x, objvals));
			
			cplex.addLe(cplex.sum(cplex.prod(-1.0, x[0]),
								  cplex.prod( 1.0, x[1]),
								  cplex.prod( 1.0, x[2])), 20.0);
			
			cplex.addLe(cplex.sum(cplex.prod( 1.0, x[0]),
								  cplex.prod(-3.0, x[1]),
								  cplex.prod( 1.0, x[2])), 30.0);
			
			if(cplex.solve()){
				
				cplex.output().println("Solution status: " + cplex.getStatus());
				cplex.output().println("Solution value: " + cplex.getObjValue());
				
				double[] val = cplex.getValues(x);
				int ncols = cplex.getNcols();
				for(int j = 0; j < ncols; j++){
					cplex.output().println("Column: " + j + "value = " + val[j]);
				}
			}
			
			cplex.end();
			
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
