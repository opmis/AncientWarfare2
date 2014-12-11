package net.shadowmage.ancientwarfare.structure.town;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.shadowmage.ancientwarfare.core.util.BlockPosition;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilder;
import net.shadowmage.ancientwarfare.structure.town.TownGenerator.TownPartBlockComparator;
import net.shadowmage.ancientwarfare.structure.town.TownTemplate.TownStructureEntry;

public class TownGeneratorStructures
{

public static void generateStructures(TownGenerator gen)
  {
  List<TownPartBlock> blocks = new ArrayList<TownPartBlock>();
  for(TownPartQuadrant tq : gen.quadrants){tq.addBlocks(blocks);}
  sortBlocksByDistance(blocks);   
  generateUniques(blocks, gen.uniqueTemplatesToGenerate, gen);
  generateMains(blocks, gen.mainTemplatesToGenerate, gen);
  generateHouses(blocks, gen.houseTemplatesToGenerate, gen);
  generateCosmetics(blocks, gen.cosmeticTemplatesToGenerate, gen);
  generateLamps(blocks, gen.template.getLamp(), gen);

  blocks.clear();
  for(TownPartQuadrant tq : gen.externalQuadrants){tq.addBlocks(blocks);}
  generateExteriorStructures(blocks, gen.exteriorTemplatesToGenerate, gen);
  }

private static void generateUniques(List<TownPartBlock> blocks, List<StructureTemplate> templatesToGenerate, TownGenerator gen)
  {
  for(TownPartBlock block : blocks)
    {
    int maxRetry = 1;//TODO base this off of townblock distance from center
    for(TownPartPlot plot : block.plots)
      {
      if(plot.closed){continue;}
      if(!plot.hasRoadBorder()){continue;}//no borders
      if(templatesToGenerate.isEmpty()){break;}
      for(int i = 0; i < maxRetry; i++)
        {
        if(templatesToGenerate.isEmpty()){break;}
        if(generateStructureForPlot(gen, plot, getRandomTemplate(templatesToGenerate, gen.rng), false)){break;}
        }
      }
    }
  }

private static void generateMains(List<TownPartBlock> blocks, List<StructureTemplate> templatesToGenerate, TownGenerator gen)
  {
  
  }

private static void generateHouses(List<TownPartBlock> blocks, List<StructureTemplate> templatesToGenerate, TownGenerator gen)
  {
  
  }

private static void generateCosmetics(List<TownPartBlock> blocks, List<StructureTemplate> templatesToGenerate, TownGenerator gen)
  {
  for(TownPartBlock block : blocks)
    {
    int maxRetry = 1;//TODO base this off of townblock distance from center
    for(TownPartPlot plot : block.plots)
      {
      if(plot.closed){continue;}        
      for(int i = 0; i < maxRetry; i++)
        {
//        if(generateCosmeticForPlot(world, plot, getRandomCosmeticTemplate())){break;}
        }
      }
    }
  }

private static void generateLamps(List<TownPartBlock> blocks, TownStructureEntry templateToGenerate, TownGenerator gen)
  {
  
  }

private static void generateExteriorStructures(List<TownPartBlock> blocks, List<StructureTemplate> templatesToGenerate, TownGenerator gen)
  {
  
  }

//************************************************* UTILITY METHODS *******************************************************//

/**
 * attempt to generate a structure at the given plot
 * @param world
 * @param plot
 * @return true if generated
 */
public static boolean generateStructureForPlot(TownGenerator gen, TownPartPlot plot, StructureTemplate template, boolean centerLength)
  {  
  int expansion = gen.template.getTownBuildingWidthExpansion();
  int face = gen.rng.nextInt(4);//select random face  
  for(int i = 0, f=face; i < 4; i++, f++)//and then iterate until a valid face is found
    {
    if(f>3){f=0;}
    if(plot.roadBorders[f])
      {
      face = f;
      break;
      }
    }
  face = (face+2)%4;//reverse face from road edge...
  int width = face==0 || face==2 ? template.xSize : template.zSize;
  int length = face==0 || face==2 ? template.zSize : template.xSize;
  if(face==0 || face==2){width+=expansion;}//temporarily expand the size of the bb by the town-template building expansion size, ensures there is room around buildings
  else{length+=expansion;}
  if(plot.getWidth()<width || plot.getLength()<length)
    {
    if(!plot.expand(width, length))
      {
      return false;
      }
    }  
  plot.markClosed();
  if(face==0 || face==2){width-=expansion;}
  else{length-=expansion;}  
  generateStructure(gen, plot, template, face, width, length, centerLength);   
  return true;
  }

/**
 * 
 * @param world the world object that is currently being generated
 * @param plot the pre-expanded plot that will have the structure generated on it
 * @param template the template to be generated
 * @param face generation orientation for the structure
 * @param width rotated structure x-dimension
 * @param length rotated structure z-dimension
 * @param center should the structure be centered in plot, or placed along road-edge?
 */
private static void generateStructure(TownGenerator gen, TownPartPlot plot, StructureTemplate template, int face, int width, int length, boolean center)
  {  
  int plotWidth = plot.getWidth();
  int plotLength = plot.getLength();
  int extraWidth = plotWidth - width;//unused width portion of the plot
  int extraLength = plotLength - length;//unused length portion of the plot
  
  int wAdj;
  int lAdj;
  
  if(center)
    {
    wAdj = extraWidth/2;    
    lAdj = extraLength/2;
    }
  else
    {
    wAdj = (face==0 || face==2) ? extraWidth/2 : face==1 ? extraWidth : 0;
    lAdj = (face==1 || face==3) ? extraLength/2 : face==2 ? extraLength : 0;
    }
    
  //find corners of the bb for the structure  
  BlockPosition min = new BlockPosition(plot.bb.min.x+wAdj, gen.townBounds.min.y, plot.bb.min.z+lAdj);
  BlockPosition max = new BlockPosition(min.x + (width-1), template.ySize, min.z+(length-1));
  StructureBB bb = new StructureBB(min, max);
  
  BlockPosition buildKey = bb.getRLCorner(face, new BlockPosition());
  buildKey.moveRight(face, template.xOffset);
  buildKey.moveBack(face, template.zOffset);  
  buildKey.y -= template.yOffset;
  bb.offset(0, -template.yOffset, 0);
  StructureBuilder b = new StructureBuilder(gen.world, template, face, buildKey, bb);
  b.instantConstruction();  
  }


//private void generateLamps(TownPartBlock block, StructureTemplate lamp)
//  {
//  int tx, tz;
//  int wx, wz;
//  int y = block.bb.min.y;
//  for(tx = block.bb.min.x; tx<=block.bb.max.x; tx++)
//    {    
//    wx = tx;
//    if(wx%4!=0){continue;}
//    wz = block.bb.min.z;
//    if(world.getBlock(wx, y, wz)==Blocks.air && world.getBlock(wx, y+1, wz)==Blocks.air)
//      {
//      generateLamp(wx, y, wz, lamp);
//      }
//    wz = block.bb.max.z;
//    if(world.getBlock(wx, y, wz)==Blocks.air && world.getBlock(wx, y+1, wz)==Blocks.air)
//      {
//      generateLamp(wx, y, wz, lamp);
//      }
//    }
//  for(tz = block.bb.min.z; tz<=block.bb.max.z; tz++)
//    {   
//    wz = tz ;
//    if(wz%4!=0){continue;}
//    wx = block.bb.min.x;
//    if(world.getBlock(wx, y, wz)==Blocks.air && world.getBlock(wx, y+1, wz)==Blocks.air)
//      {
//      generateLamp(wx, y, wz, lamp);
//      }
//    wx = block.bb.max.x;
//    if(world.getBlock(wx, y, wz)==Blocks.air && world.getBlock(wx, y+1, wz)==Blocks.air)
//      {
//      generateLamp(wx, y, wz, lamp);
//      }
//    }
//  }

//private void generateStructures(World world, TownPartBlock block)
//  {  
//  int maxRetry = 1;//TODO base this off of townblock distance from center
//  for(TownPartPlot plot : block.plots)
//    {
//    if(plot.closed){continue;}
//    if(!plot.hasRoadBorder()){continue;}//no borders
//    if(templatesToGenerate.isEmpty()){break;}
//    for(int i = 0; i < maxRetry; i++)
//      {
//      if(templatesToGenerate.isEmpty()){break;}
//      if(generateStructureForPlot(world, plot, getRandomTemplate())){break;}
//      }
//    }
//  }


///**
// * attempt to generate a structure at the given plot
// * @param world
// * @param plot
// * @return true if generated
// */
//private boolean generateCosmeticForPlot(World world, TownPartPlot plot, StructureTemplate template)
//  {  
//  int expansion = this.template.getTownBuildingWidthExpansion();
//  int face = rng.nextInt(4);//select random face  
//  int width = face==0 || face==2 ? template.xSize : template.zSize;
//  int length = face==0 || face==2 ? template.zSize : template.xSize;
//  width+=expansion;
//  length+=expansion;
//  if(plot.getWidth()<width || plot.getLength()<length)
//    {
//    if(!expandPlot(plot, width, length))
//      {
//      return false;
//      }
//    }  
//  plot.markClosed();
//  width-=expansion;
//  length-=expansion;
//  generateStructure(world, plot, template, face, width, length, true); 
//  return true;
//  }


/**
 * pull a random template from the input generation list, does not remove
 * @return
 */
public static StructureTemplate getRandomTemplate(List<StructureTemplate> templatesToGenerate, Random rng)
  {
  if(templatesToGenerate.size()==0){return null;}
  int roll = rng.nextInt(templatesToGenerate.size());
  return templatesToGenerate.get(roll);
  }

public static void sortBlocksByDistance(List<TownPartBlock> blocks)
  {
  Collections.sort(blocks, new TownPartBlockComparator());
  }
}