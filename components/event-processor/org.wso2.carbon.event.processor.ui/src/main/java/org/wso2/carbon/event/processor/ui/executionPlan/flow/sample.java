package org.wso2.carbon.event.processor.ui.executionPlan.flow;

import org.wso2.carbon.event.processor.ui.executionPlan.flow.siddhi.visitor.SiddhiFlowCompiler;

/**
 * Created by janaki on 2/12/16.
 */
public class sample {
    public static void main(String args[]) {
        String ex = (
               "/* Enter a unique ExecutionPlan */\n" +
                       "@Plan:name('PersistedBlackListProcessor')\n" +
                       "\n" +
                       "/* Enter a unique description for ExecutionPlan */\n" +
                       "-- @Plan:description('ExecutionPlan')\n" +
                       "\n" +
                       "/* define streams/tables and write queries here ... */\n" +
                       "\n" +
                       "@Import('DeleteAllUsers:1.0.0')\n" +
                       "define stream DeleteAllUsers (deleteAll bool);\n" +
                       "\n" +
                       "@Import('BlackListStream:1.0.0')\n" +
                       "define stream BlackListStream (cardNo string);\n" +
                       "\n" +
                       "@Import('CardUserStream:1.0.0')\n" +
                       "define stream CardUserStream (name string, cardNum string, blacklisted bool);\n" +
                       "\n" +
                       "@Import('PurchaseStream:1.0.0')\n" +
                       "define stream PurchaseStream (price double, cardNo string, place string);\n" +
                       "\n" +
                       "@Export('WhiteListPurchaseStream:1.0.0')\n" +
                       "define stream WhiteListPurchaseStream (cardNo string, name string, price double);\n" +
                       "\n" +
                       "@from(eventtable = 'rdbms' , datasource.name = 'WSO2_CARBON_DB' , table.name = 'CEPSample0107CardUserTable')\n" +
                       "define table CardUserTable (name string, cardNum string, blacklisted bool) ; \n" +
                       "\n" +
                       "from CardUserStream\n" +
                       "select * \n" +
                       "insert into CardUserTable;\n" +
                       "\t\n" +
                       "from BlackListStream\n" +
                       "select cardNo as cardNum, true as blacklisted  \n" +
                       "update CardUserTable\n" +
                       "\ton cardNum == CardUserTable.cardNum;\n" +
                       "\n" +
                       "from DeleteAllUsers[deleteAll == true] \n" +
                       "delete CardUserTable\n" +
                       "\ton deleteAll == CardUserTable.blacklisted or deleteAll != CardUserTable.blacklisted ;\n" +
                       "\t\n" +
                       "from PurchaseStream#window.length(1) as p join CardUserTable as c\n" +
                       "\ton  p.cardNo == c.cardNum\n" +
                       "select p.cardNo as cardNo, c.name as name, p.price as price\n" +
                       "having c.blacklisted == false\n" +
                       "insert into WhiteListPurchaseStream ;"
        );

        SiddhiFlowCompiler s= new SiddhiFlowCompiler();
        StringBuilder e = s.parseString(ex);

        System.out.println(e);

        ExecutionPlanFlow executionPlanFlow = new ExecutionPlanFlow();

        System.out.println("\n");
        System.out.print(executionPlanFlow.getExecutionPlanFlow(ex));
    }

}
