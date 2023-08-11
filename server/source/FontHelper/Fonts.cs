using Figgle;
using Harbinger.EventsBus;
using Harbinger.EventsBus.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Harbinger.FontHelper
{
    public class Fonts
    {
        public static void init()
        {
            /* OneRow */
            AddFont("1row");
            /* ThreeD */
            AddFont("3-d");
            /* ThreeDDiagonal */
            AddFont("3d_diagonal");
            /* ThreeByFive */
            AddFont("3x5");
            /* FourMax */
            AddFont("4max");
            /* FiveLineOblique */
            AddFont("5lineoblique");
            /* Acrobatic */
            AddFont("acrobatic");
            /* Alligator */
            AddFont("alligator");
            /* Alligator2 */
            AddFont("alligator2");
            /* Alligator3 */
            AddFont("alligator3");
            /* Alpha */
            AddFont("alpha");
            /* Alphabet */
            AddFont("alphabet");
            /* Amc3Line */
            AddFont("amc3line");
            /* Amc3Liv1 */
            AddFont("amc3liv1");
            /* AmcAaa01 */
            AddFont("amcaaa01");
            /* AmcNeko */
            AddFont("amcneko");
            /* AmcRazor2 */
            AddFont("amcrazo2");
            /* AmcRazor */
            AddFont("amcrazor");
            /* AmcSlash */
            AddFont("amcslash");
            /* AmcSlder */
            AddFont("amcslder");
            /* AmcThin */
            AddFont("amcthin");
            /* AmcTubes */
            AddFont("amctubes");
            /* AmcUn1 */
            AddFont("amcun1");
            /* Arrows */
            AddFont("arrows");
            /* AsciiNewroman */
            AddFont("ascii_new_roman");
            /* Avatar */
            AddFont("avatar");
            /* B1FF */
            AddFont("B1FF");
            /* Banner */
            AddFont("banner");
            /* Banner3 */
            AddFont("banner3");
            /* Banner3D */
            AddFont("banner3-D");
            /* Banner4 */
            AddFont("banner4");
            /* BarbWire */
            AddFont("barbwire");
            /* Basic */
            AddFont("basic");
            /* Bear */
            AddFont("bear");
            /* Bell */
            AddFont("bell");
            /* Benjamin */
            AddFont("benjamin");
            /* Big */
            AddFont("big");
            /* BigChief */
            AddFont("bigchief");
            /* BigFig */
            AddFont("bigfig");
            /* Binary */
            AddFont("binary");
            /* Block */
            AddFont("block");
            /* Blocks */
            AddFont("blocks");
            /* Bolger */
            AddFont("bolger");
            /* Braced */
            AddFont("braced");
            /* Bright */
            AddFont("bright");
            /* Broadway */
            AddFont("broadway");
            /* BroadwayKB */
            AddFont("broadway_kb");
            /* Bubble */
            AddFont("bubble");
            /* Bulbhead */
            AddFont("bulbhead");
            /* Caligraphy2 */
            AddFont("calgphy2");
            /* Caligraphy */
            AddFont("caligraphy");
            /* Cards */
            AddFont("cards");
            /* CatWalk */
            AddFont("catwalk");
            /* Chiseled */
            AddFont("chiseled");
            /* Chunky */
            AddFont("chunky");
            /* Coinstak */
            AddFont("coinstak");
            /* Cola */
            AddFont("cola");
            /* Colossal */
            AddFont("colossal");
            /* Computer */
            AddFont("computer");
            /* Contessa */
            AddFont("contessa");
            /* Contrast */
            AddFont("contrast");
            /* Cosmic */
            AddFont("cosmic");
            /* Cosmike */
            AddFont("cosmike");
            /* Crawford */
            AddFont("crawford");
            /* Crazy */
            AddFont("crazy");
            /* Cricket */
            AddFont("cricket");
            /* Cursive */
            AddFont("cursive");
            /* CyberLarge */
            AddFont("cyberlarge");
            /* CyberMedium */
            AddFont("cybermedium");
            /* CyberSmall */
            AddFont("cybersmall");
            /* Cygnet */
            AddFont("cygnet");
            /* DANC4 */
            AddFont("DANC4");
            /* DancingFont */
            AddFont("dancingfont");
            /* Decimal */
            AddFont("decimal");
            /* DefLeppard */
            AddFont("defleppard");
            /* Diamond */
            AddFont("diamond");
            /* DietCola */
            AddFont("dietcola");
            /* Digital */
            AddFont("digital");
            /* Doh */
            AddFont("doh");
            /* Doom */
            AddFont("doom");
            /* DosRebel */
            AddFont("dosrebel");
            /* DotMatrix */
            AddFont("dotmatrix");
            /* Double */
            AddFont("double");
            /* DoubleShorts */
            AddFont("doubleshorts");
            /* DRPepper */
            AddFont("drpepper");
            /* DWhistled */
            AddFont("dwhistled");
            /* EftiChess */
            AddFont("eftichess");
            /* EftiFont */
            AddFont("eftifont");
            /* EftiPiti */
            AddFont("eftipiti");
            /* EftiRobot */
            AddFont("eftirobot");
            /* EftiItalic */
            AddFont("eftitalic");
            /* EftiWall */
            AddFont("eftiwall");
            /* EftiWater */
            AddFont("eftiwater");
            /* Epic */
            AddFont("epic");
            /* Fender */
            AddFont("fender");
            /* Filter */
            AddFont("filter");
            /* FireFontK */
            AddFont("fire_font-k");
            /* FireFontS */
            AddFont("fire_font-s");
            /* Flipped */
            AddFont("flipped");
            /* FlowerPower */
            AddFont("flowerpower");
            /* FourTops */
            AddFont("fourtops");
            /* Fraktur */
            AddFont("fraktur");
            /* FunFace */
            AddFont("funface");
            /* FunFaces */
            AddFont("funfaces");
            /* Fuzzy */
            AddFont("fuzzy");
            /* Georgia16 */
            AddFont("georgi16");
            /* Georgia11 */
            AddFont("Georgia11");
            /* Ghost */
            AddFont("ghost");
            /* Ghoulish */
            AddFont("ghoulish");
            /* Glenyn */
            AddFont("glenyn");
            /* Goofy */
            AddFont("goofy");
            /* Gothic */
            AddFont("gothic");
            /* Graceful */
            AddFont("graceful");
            /* Gradient */
            AddFont("gradient");
            /* Graffiti */
            AddFont("graffiti");
            /* Greek */
            AddFont("greek");
            /* HeartLeft */
            AddFont("heart_left");
            /* HeartRight */
            AddFont("heart_right");
            /* Henry3d */
            AddFont("henry3d");
            /* Hex */
            AddFont("hex");
            /* Hieroglyphs */
            AddFont("hieroglyphs");
            /* Hollywood */
            AddFont("hollywood");
            /* HorizontalLeft */
            AddFont("horizontalleft");
            /* HorizontalRight */
            AddFont("horizontalright");
            /* ICL1900 */
            AddFont("ICL-1900");
            /* Impossible */
            AddFont("impossible");
            /* Invita */
            AddFont("invita");
            /* Isometric1 */
            AddFont("isometric1");
            /* Isometric2 */
            AddFont("isometric2");
            /* Isometric3 */
            AddFont("isometric3");
            /* Isometric4 */
            AddFont("isometric4");
            /* Italic */
            AddFont("italic");
            /* Ivrit */
            AddFont("ivrit");
            /* Jacky */
            AddFont("jacky");
            /* Jazmine */
            AddFont("jazmine");
            /* Jerusalem */
            AddFont("jerusalem");
            /* Katakana */
            AddFont("katakana");
            /* Kban */
            AddFont("kban");
            /* Keyboard */
            AddFont("keyboard");
            /* Knob */
            AddFont("knob");
            /* Konto */
            AddFont("konto");
            /* KontoSlant */
            AddFont("kontoslant");
            /* Larry3d */
            AddFont("larry3d");
            /* Lcd */
            AddFont("lcd");
            /* Lean */
            AddFont("lean");
            /* Letters */
            AddFont("letters");
            /* LilDevil */
            AddFont("lildevil");
            /* LineBlocks */
            AddFont("lineblocks");
            /* Linux */
            AddFont("linux");
            /* LockerGnome */
            AddFont("lockergnome");
            /* Madrid */
            AddFont("madrid");
            /* Marquee */
            AddFont("marquee");
            /* MaxFour */
            AddFont("maxfour");
            /* Merlin1 */
            AddFont("merlin1");
            /* Merlin2 */
            AddFont("merlin2");
            /* Mike */
            AddFont("mike");
            /* Mini */
            AddFont("mini");
            /* Mirror */
            AddFont("mirror");
            /* Mnemonic */
            AddFont("mnemonic");
            /* Modular */
            AddFont("modular");
            /* Morse */
            AddFont("morse");
            /* Morse2 */
            AddFont("morse2");
            /* Moscow */
            AddFont("moscow");
            /* Mshebrew210 */
            AddFont("mshebrew210");
            /* Muzzle */
            AddFont("muzzle");
            /* NancyJ */
            AddFont("nancyj");
            /* NancyJFancy */
            AddFont("nancyj-fancy");
            /* NancyJImproved */
            AddFont("nancyj-improved");
            /* NancyJUnderlined */
            AddFont("nancyj-underlined");
            /* Nipples */
            AddFont("nipples");
            /* NScript */
            AddFont("nscript");
            /* NTGreek */
            AddFont("ntgreek");
            /* NVScript */
            AddFont("nvscript");
            /* O8 */
            AddFont("o8");
            /* Octal */
            AddFont("octal");
            /* Ogre */
            AddFont("ogre");
            /* OldBanner */
            AddFont("oldbanner");
            /* OS2 */
            AddFont("os2");
            /* Pawp */
            AddFont("pawp");
            /* Peaks */
            AddFont("peaks");
            /* PeaksSlant */
            AddFont("peaksslant");
            /* Pebbles */
            AddFont("pebbles");
            /* Pepper */
            AddFont("pepper");
            /* Poison */
            AddFont("poison");
            /* Puffy */
            AddFont("puffy");
            /* Puzzle */
            AddFont("puzzle");
            /* Pyramid */
            AddFont("pyramid");
            /* Rammstein */
            AddFont("rammstein");
            /* Rectangles */
            AddFont("rectangles");
            /* RedPhoenix */
            AddFont("red_phoenix");
            /* Relief */
            AddFont("relief");
            /* Relief2 */
            AddFont("relief2");
            /* Rev */
            AddFont("rev");
            /* Reverse */
            AddFont("reverse");
            /* Roman */
            AddFont("roman");
            /* Rot13 */
            AddFont("rot13");
            /* Rotated */
            AddFont("rotated");
            /* Rounded */
            AddFont("rounded");
            /* RowanCap */
            AddFont("rowancap");
            /* Rozzo */
            AddFont("rozzo");
            /* Runic */
            AddFont("runic");
            /* Runyc */
            AddFont("runyc");
            /* SantaClara */
            AddFont("santaclara");
            /* SBlood */
            AddFont("sblood");
            /* Script */
            AddFont("script");
            /* ScriptSlant */
            AddFont("slscript");
            /* SerifCap */
            AddFont("serifcap");
            /* Shadow */
            AddFont("shadow");
            /* Shimrod */
            AddFont("shimrod");
            /* Short */
            AddFont("short");
            /* Slant */
            AddFont("slant");
            /* Slide */
            AddFont("slide");
            /* Small */
            AddFont("small");
            /* SmallCaps */
            AddFont("smallcaps");
            /* IsometricSmall */
            AddFont("smisome1");
            /* KeyboardSmall */
            AddFont("smkeyboard");
            /* PoisonSmall */
            AddFont("smpoison");
            /* ScriptSmall */
            AddFont("smscript");
            /* ShadowSmall */
            AddFont("smshadow");
            /* SlantSmall */
            AddFont("smslant");
            /* TengwarSmall */
            AddFont("smtengwar");
            /* Soft */
            AddFont("soft");
            /* Speed */
            AddFont("speed");
            /* Spliff */
            AddFont("spliff");
            /* SRelief */
            AddFont("s-relief");
            /* Stacey */
            AddFont("stacey");
            /* Stampate */
            AddFont("stampate");
            /* Stampatello */
            AddFont("stampatello");
            /* Standard */
            AddFont("standard");
            /* Starstrips */
            AddFont("starstrips");
            /* Starwars */
            AddFont("starwars");
            /* Stellar */
            AddFont("stellar");
            /* Stforek */
            AddFont("stforek");
            /* Stop */
            AddFont("stop");
            /* Straight */
            AddFont("straight");
            /* SubZero */
            AddFont("sub-zero");
            /* Swampland */
            AddFont("swampland");
            /* Swan */
            AddFont("swan");
            /* Sweet */
            AddFont("sweet");
            /* Tanja */
            AddFont("tanja");
            /* Tengwar */
            AddFont("tengwar");
            /* Term */
            AddFont("term");
            /* Test1 */
            AddFont("test1");
            /* Thick */
            AddFont("thick");
            /* Thin */
            AddFont("thin");
            /* ThreePoint */
            AddFont("threepoint");
            /* Ticks */
            AddFont("ticks");
            /* TicksSlant */
            AddFont("ticksslant");
            /* Tiles */
            AddFont("tiles");
            /* TinkerToy */
            AddFont("tinker-toy");
            /* Tombstone */
            AddFont("tombstone");
            /* Train */
            AddFont("train");
            /* Trek */
            AddFont("trek");
            /* Tsalagi */
            AddFont("tsalagi");
            /* Tubular */
            AddFont("tubular");
            /* Twisted */
            AddFont("twisted");
            /* TwoPoint */
            AddFont("twopoint");
            /* Univers */
            AddFont("univers");
            /* UsaFlag */
            AddFont("usaflag");
            /* Varsity */
            AddFont("varsity");
            /* Wavy */
            AddFont("wavy");
            /* Weird */
            AddFont("weird");
            /* WetLetter */
            AddFont("wetletter");
            /* Whimsy */
            AddFont("whimsy");
            /* Wow */
            AddFont("wow");

        }

        internal static Dictionary<string, FiggleFont> fonts = new();

        private static void AddFont(string v)
        {
            var font = FiggleFonts.TryGetByName(v);
            if (font == null) return;
            fonts.Add(v, font);
        }

        public List<string> FontNames
        {
            get
            {
                return fonts.Keys.ToList();
            }
        }

        public static string RenderUsing(string v, string text)
        {
            return fonts[v].Render(text);
        }

        /// <summary>
        /// A single-shot event for the FontHelper executable
        /// </summary>

        [Subscribe(Priority.Uncategorizable, true)]
        public static void onStartup(StartupEvent evt)
        {
            init();
            Console.Clear();
            Console.Title = $"Harbinger - Font Helper - {GitVersion.FullVersion}";
            Console.BackgroundColor = ConsoleColor.Black;
            Console.ForegroundColor = ConsoleColor.DarkGreen;

            Console.WriteLine(RenderUsing("banner3-D", "Harbinger"));
            Console.WriteLine(RenderUsing("slant", "FontHelper"));

            Console.Write("\nTo use a specific font, enter it here, otherwise press enter to iterate over all fonts: ");
            string fontName = Console.ReadLine();


            Console.Write("\nWhat text do you want to display? Leave blank to write the name of the font: ");
            string text = Console.ReadLine();

            if(fontName.Length==0)
            {
                // iterate!!
                foreach(var font in fonts)
                {
                    try
                    {
                        Console.WriteLine(font.Value.Render((text.Length > 0 ? text : font.Key)));
                        Console.WriteLine(font.Key);
                        Thread.Sleep(250);
                    }catch(Exception ex) { }
                }
            }else
            {
                if(fonts.ContainsKey(fontName))
                {
                    Console.WriteLine(RenderUsing(fontName, (text.Length > 0 ? text : fontName)));

                }
            }

        }
    }
}
