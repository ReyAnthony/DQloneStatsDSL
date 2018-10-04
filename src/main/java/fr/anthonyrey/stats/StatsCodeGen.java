package fr.anthonyrey.stats;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StatsCodeGen implements statsDSLListener {

    private String filename;
    private Path savePath;
    private String input;
    private final Map<String, ClassData> classes;
    private String currentClass;
    private ClassData currentClassData;

    public StatsCodeGen(String filename, Path savePath, String input) {
        this.filename = filename;
        this.savePath = savePath;
        this.input = input;

        classes = new HashMap<>();
    }

    public void start() {
        final CodePointCharStream input = CharStreams.fromString(this.input);

        final statsDSLLexer lexer = new statsDSLLexer(input);
        final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        final statsDSLParser parser = new statsDSLParser(tokenStream);
        final statsDSLParser.ProgramContext program = parser.program();
        ParseTreeWalker.DEFAULT.walk(this, program);
    }

    @Override
    public void enterProgram(statsDSLParser.ProgramContext ctx) {
        System.out.println("Running DQloneStatsDSL codegen");
    }

    @Override
    public void exitProgram(statsDSLParser.ProgramContext ctx) {

        final String cFile = savePath.toString() + "/" + filename + ".c";
        final String hFile = savePath.toString() + "/" + filename + ".h";

        try (PrintWriter c_file = new PrintWriter(cFile, "UTF-8");
             PrintWriter h_file = new PrintWriter(hFile, "UTF-8")){

            StringBuffer sb = new StringBuffer();

            //codegen.h
            sb.append("//Code generated by DQloneStatsDSL on ").append(LocalDateTime.now()).append("\n");
            sb.append("#ifndef STATS_ENGINE_CODEGEN_H\n");
            sb.append("#define STATS_ENGINE_CODEGEN_H\n");
            sb.append("#include \"types.h\"\n");
            sb.append("#include \"assert.h\"\n");
            sb.append("typedef enum {");
            final Iterator<String> iterator = classes.keySet().iterator();
            while(iterator.hasNext()) {
                sb.append(iterator.next());
                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append("} class_t;").append("\n");

            //TODO need something to define the existing types
            sb.append("typedef struct {\n" +
                    "     class_t class;\n" +
                    "     \n" +
                    "     uint_16 hp;\n" +
                    "     uint_16 agi;\n" +
                    "     uint_16 atk;\n" +
                    "     uint_16 def;\n" +
                    "     uint_16 mag;\n" +
                    "     \n" +
                    "     uint_32 xp;   \n" +
                    "     uint_8 level;\n" +
                    "     \n" +
                    "} stats_t;").append("\n");

            sb.append("uint_32 get_nb_points_for_level(class_t class, uint_8 level);\n");
            sb.append("void increase_stats(stats_t* stats);\n");
            sb.append("#endif\n");

            //System.out.println(sb.toString());
            h_file.write(sb.toString());
            System.out.println("Wrote " + hFile);

            //codegen.c
            sb.delete(0, sb.length());

            sb.append("//Code generated by DQloneStatsDSL on ").append(LocalDateTime.now()).append("\n");
            sb.append("#include ").append("\"stats_engine_codegen.h\"").append("\n");
            sb.append("static void set_stats(stats_t* stats, uint_16 hp, uint_16 agi, uint_16 atk, uint_16 def, uint_16 mag);\n");
            sb.append("static void set_stats(stats_t* stats, uint_16 hp, uint_16 agi, uint_16 atk, uint_16 def, uint_16 mag){\n");
            sb.append("    ");
            sb.append("stats->hp = hp").append(";\n");
            sb.append("    ");
            sb.append("stats->agi = agi").append(";\n");
            sb.append("    ");
            sb.append("stats->atk = atk").append(";\n");
            sb.append("    ");
            sb.append("stats->def = def").append(";\n");
            sb.append("    ");
            sb.append("stats->mag = mag").append(";\n");;
            sb.append("}\n");
            sb.append("uint_32 get_nb_points_for_level(class_t class, uint_8 level) {").append("\n");
            sb.append("    ").append("assert(level > 0);\n");
            classes.forEach( (c, classData) -> {
                sb.append("    ");
                sb.append("if (class == ").append(c).append("){\n");
                classData.getStatsData().forEach(statsData -> {
                    sb.append("    ").append("    ");
                    sb.append("if (level == ").append(statsData.getForLevel()).append(") { ");
                    sb.append("return ").append(statsData.getNeededXp()).append(";").append(" }\n");
                });
                sb.append("    ").append("}").append("\n");
            });
            sb.append("    ").append("return -1;").append("\n");
            sb.append("}").append("\n");

            sb.append("void increase_stats(stats_t* stats) {\n");
            classes.forEach( (c, classData) -> {
                sb.append("    ");
                sb.append("if (stats->class == ").append(c).append("){\n");
                classData.getStatsData().forEach(statsData -> {
                    sb.append("    ").append("    ");
                    sb.append("if (stats->level == ").append(statsData.getForLevel()).append(")").append(" { ");
                    sb.append("set_stats(stats,");
                    sb.append(statsData.getHp()).append(",");
                    sb.append(statsData.getAgi()).append(",");
                    sb.append(statsData.getAtk()).append(",");
                    sb.append(statsData.getDef()).append(",");
                    sb.append(statsData.getMag()).append(");");
                    sb.append(" return; ");
                    sb.append("}\n");
                });
                sb.append("    ").append("}").append("\n");
            });

            sb.append("}");
            //System.out.println(sb.toString());
            c_file.write(sb.toString());
            System.out.println("Wrote " + cFile);
            System.out.println("Done.");

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void enterClassDef(statsDSLParser.ClassDefContext ctx) {
        currentClass = ctx.ID().getText();
    }

    @Override
    public void exitClassDef(statsDSLParser.ClassDefContext ctx) {

        if(!currentClassData.isValid()){
            throw new RuntimeException("class definition " + ctx.ID().getSymbol().getText() + " is not valid");
        }
        this.classes.put(currentClass, currentClassData);
    }

    @Override
    public void enterInnerClassDef(statsDSLParser.InnerClassDefContext ctx) {
        currentClassData = new ClassData();
    }

    @Override
    public void exitInnerClassDef(statsDSLParser.InnerClassDefContext ctx) {
        classes.put(currentClass, currentClassData);
    }

    @Override
    public void enterInitStat(statsDSLParser.InitStatContext ctx) {

        final StatsData statsData = new StatsData();

        ctx.statDef().forEach((statDefContext) -> {

            final String number = statDefContext.NUMBER().getText();

            if (statDefContext.stat().AGI() != null) {
                statsData.setAgi(number);
            }
            else if (statDefContext.stat().ATK() != null) {
                statsData.setAtk(number);
            }
            else if (statDefContext.stat().DEF() != null) {
                statsData.setDef(number);
            }
            else if (statDefContext.stat().MAG() != null) {
                statsData.setMag(number);
            }
            else if (statDefContext.stat().HP() != null) {
                statsData.setHp(number);
            }
        });

        statsData.setForLevel("1");
        statsData.setNeededXp("0");
        if(!statsData.isValid()) {
            throw new RuntimeException("Error in stats data at " + ctx.getText());
        }

        currentClassData.addStatsData(statsData);
    }

    @Override
    public void exitInitStat(statsDSLParser.InitStatContext ctx) {

    }

    @Override
    public void enterInitLevel(statsDSLParser.InitLevelContext ctx) {

        //TODO remove duped code
        final StatsData statsData = new StatsData();

        ctx.statDef().forEach((statDefContext) -> {

            final String number = statDefContext.NUMBER().getText();

            if (statDefContext.stat().AGI() != null) {
                statsData.setAgi(number);
            }
            else if (statDefContext.stat().ATK() != null) {
                statsData.setAtk(number);
            }
            else if (statDefContext.stat().DEF() != null) {
                statsData.setDef(number);
            }
            else if (statDefContext.stat().MAG() != null) {
                statsData.setMag(number);
            }
            else if (statDefContext.stat().HP() != null) {
                statsData.setHp(number);
            }
        });

        statsData.setForLevel(ctx.NUMBER().getText());
        statsData.setNeededXp(ctx.xpDef().NUMBER().getText());
        if(!statsData.isValid()) {
            throw new RuntimeException("Error in stats data at " + ctx.getText());
        }

        currentClassData.addStatsData(statsData);
    }

    @Override
    public void exitInitLevel(statsDSLParser.InitLevelContext ctx) {

    }

    @Override
    public void enterXpDef(statsDSLParser.XpDefContext ctx) {

    }

    @Override
    public void exitXpDef(statsDSLParser.XpDefContext ctx) {

    }

    @Override
    public void enterStatDef(statsDSLParser.StatDefContext ctx) {

    }

    @Override
    public void exitStatDef(statsDSLParser.StatDefContext ctx) {

    }

    @Override
    public void enterStat(statsDSLParser.StatContext ctx) {

    }

    @Override
    public void exitStat(statsDSLParser.StatContext ctx) {

    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {

    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {

    }
}