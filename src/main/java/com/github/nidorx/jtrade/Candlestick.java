package com.github.nidorx.jtrade;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Padrões de Candlestick
 *
 * @author Alex
 */
public final class Candlestick {

    /**
     * Cache de processamento imediato de listas. Evita o reprocessamento desnecssário no curto intervalo de tempo
     */
    private static final Map<String, Double> CACHE_RESULT = new LinkedHashMap(21, .75F, true) {
        @Override
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 20;
        }
    };

    /**
     * Padrão de Candles Brancos
     */
    private static final Predicate<OHLC> WHITE = (ohlc) -> ohlc.open < ohlc.close;

    /**
     * Padrão de Candles negros
     */
    private static final Predicate<OHLC> BLACK = (ohlc) -> ohlc.open > ohlc.close;

    /**
     * Padrão de Candle que possui sombra superior
     */
    private static final Predicate<OHLC> UPPER_SHADOW = (ohlc) -> ohlc.high > Math.max(ohlc.open, ohlc.close);

    /**
     * Padrão de Candle que possui sombra inferior
     */
    private static final Predicate<OHLC> LOWER_SHADOW = (ohlc) -> ohlc.low < Math.min(ohlc.open, ohlc.close);

    /**
     * Padrão de Candle que possui sombra superior e inferior
     */
    private static final Predicate<OHLC> ALL_SHADOW = UPPER_SHADOW.and(LOWER_SHADOW);

    /**
     * Padrão de Candle que possui ALGUMA sombra superior e inferior
     */
    private static final Predicate<OHLC> ANY_SHADOW = UPPER_SHADOW.or(LOWER_SHADOW);

    /**
     * Padrão de Candle que NÃO POSSUI sombra
     */
    private static final Predicate<OHLC> NO_SHADOW = ANY_SHADOW.negate();

    /**
     * Padrão de Candle onde a sombra superior é maior do que o corpo
     */
    private static final Predicate<OHLC> UPPER_SHADOW_LARGER_THAN_BODY = UPPER_SHADOW.and((ohlc) -> {
        return (ohlc.high - Math.max(ohlc.open, ohlc.close)) > body(ohlc);
    });

    /**
     * Padrão de Candle onde a sombra inferior é maior do que o corpo
     */
    private static final Predicate<OHLC> LOWER_SHADOW_LARGER_THAN_BODY = LOWER_SHADOW.and((ohlc) -> {
        return (Math.min(ohlc.open, ohlc.close) - ohlc.low) > body(ohlc);
    });

    /**
     * Padrão de Candle onde TODAS as sombras são maiores do que o corpo
     */
    private static final Predicate<OHLC> ALL_SHADOW_LARGER_THAN_BODY
            = UPPER_SHADOW_LARGER_THAN_BODY.and(LOWER_SHADOW_LARGER_THAN_BODY);

    /**
     * Padrão de Candle onde ALGUMA as sombras são maiores do que o corpo
     */
    private static final Predicate<OHLC> ANY_SHADOW_LARGER_THAN_BODY
            = UPPER_SHADOW_LARGER_THAN_BODY.or(LOWER_SHADOW_LARGER_THAN_BODY);

    /**
     * Padrão de Candle onde NENHUMA das sombras é maior do que o corpo
     */
    private static final Predicate<OHLC> NO_SHADOW_LARGER_THAN_BODY = ANY_SHADOW_LARGER_THAN_BODY.negate();

    /**
     * Verifica se o item atual da lista de valores é um LONG LINE.
     * <p>
     * Definição: Um candle é considerado um LONG LINE quando o seu tamanho é MAIOR OU IGUAL a 70% da variação da
     * volatilidade dos ultimos 25 candles.
     * <p>
     * A volatilidade é a Média exponencial dos tamanhos dos candles individuais para o período dos ultimos 25 candles.
     *
     * @see Candlestick#averageDistance(java.util.List, int)
     * @see Candlestick#size(info.alexrodin.ta.OHLC)
     */
    private static final Predicate<List<OHLC>> LONG_LINE = (rates) -> {
        double avg = averageDistance(rates, 25);
        final OHLC ohlc = rates.get(0);
        return size(ohlc) >= avg * 0.7;
    };

    /**
     * Verifica se o item atual da lista de valores é um Short Line.
     * <p>
     * Definição: Um candle é considerado um SHORT LINE quando o seu tamanho MENOR QUE 70% da variação da volatilidade
     * dos ultimos 25 candles.
     * <p>
     * A volatilidade é a Média exponencial dos tamanhos dos candles individuais para o período dos ultimos 25 candles.
     *
     * @see Candlestick#LONG_LINE
     * @see Candlestick#averageDistance(java.util.List, int)
     * @see Candlestick#size(info.alexrodin.ta.OHLC)
     */
    private static final Predicate<List<OHLC>> SHORT_LINE = LONG_LINE.negate();

    /**
     * O corpo da vela é três vezes maior do que a média do tamanho dos corpos das últimas 5 ou 10 velas
     */
    private static final Predicate<List<OHLC>> LONG_CANDLE = (rates) -> {
        final int avgPeriod = 10;

        if (rates.size() < avgPeriod) {
            // Não possui registros suficientes para verificar o padrão
            return false;
        }

        return body(rates.get(0)) >= averageBody(rates, avgPeriod) * 3;
    };

    /**
     * Long White Candle
     * <p>
     * Oposto de {@link Candlestick#LONG_BLACK_CANDLE}
     * <p>
     * Formação do padrão:
     * <ul>
     * <li> Corpo branco
     * <li> Sombras superiores e inferiores
     * <li> Nenhuma das sombras pode ser maior do que o corpo
     * <li> O corpo da vela é três vezes maior do que a média do tamanho dos corpos das últimas 5 ou 10 velas
     * <li> Aparece como uma {@link Candlestick#LONG_LINE linha longa}
     * </ul>
     *
     * @see http://www.candlescanner.com/candlestick-patterns/long-white-candle/
     */
    public static final Predicate<List<OHLC>> LONG_WHITE_CANDLE = (rates) -> {
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░┌┴┐░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│░│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│░│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│░│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│░│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│░│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│░│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░└┬┘░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        return WHITE
                .and(ALL_SHADOW)
                .and(NO_SHADOW_LARGER_THAN_BODY)
                .and((e) -> LONG_CANDLE.test(rates))
                .and((t) -> LONG_LINE.test(rates))
                .test(rates.get(0));
    };

    /**
     * Long Black Candle
     * <p>
     * Oposto de {@link Candlestick#LONG_WHITE_CANDLE}
     * <p>
     * Formação do padrão:
     * <ul>
     * <li> Corpo preto
     * <li> Sombras superiores e inferiores
     * <li> Nenhuma das sombras pode ser maior do que o corpo
     * <li> O corpo da vela é três vezes maior do que a média do tamanho dos corpos das últimas 5 ou 10 velas
     * <li> Aparece como uma {@link Candlestick#LONG_LINE linha longa}
     * </ul>
     *
     * @see http://www.candlescanner.com/candlestick-patterns/long-black-candle/
     */
    public static final Predicate<List<OHLC>> LONG_BLACK_CANDLE = (rates) -> {
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░┌┴┐░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│▓│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│▓│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│▓│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│▓│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│▓│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│▓│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░└┬┘░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        return BLACK
                .and(ALL_SHADOW)
                .and(NO_SHADOW_LARGER_THAN_BODY)
                .and((e) -> LONG_CANDLE.test(rates))
                .and((t) -> LONG_LINE.test(rates))
                .test(rates.get(0));
    };

    /**
     * White Candle
     * <p>
     * Oposto de {@link Candlestick#BLACK_CANDLE}
     * <p>
     * Formação do padrão:
     * <ul>
     * <li> Corpo branco
     * <li> Sombras superiores e inferiores
     * <li> Nenhuma das sombras pode ser maior do que o corpo
     * <li> Aparece como uma {@link Candlestick#LONG_LINE linha longa}
     * <li> NÃO SER UM {@link Candlestick#LONG_WHITE_CANDLE}
     * </ul>
     *
     * @see http://www.candlescanner.com/candlestick-patterns/long-black-candle/
     */
    public static final Predicate<List<OHLC>> WHITE_CANDLE = (rates) -> {
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░┌┴┐░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│░│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│░│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│░│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│░│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░└┬┘░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        return WHITE
                .and(ALL_SHADOW)
                .and(NO_SHADOW_LARGER_THAN_BODY)
                .and((t) -> LONG_LINE.test(rates))
                .and((e) -> LONG_CANDLE.negate().test(rates))
                .test(rates.get(0));
    };

    /**
     * Black Candle
     * <p>
     * Oposto de {@link Candlestick#WHITE_CANDLE}
     * <p>
     * Formação do padrão:
     * <ul>
     * <li> Corpo preto
     * <li> Sombras superiores e inferiores
     * <li> Nenhuma das sombras pode ser maior do que o corpo
     * <li> Aparece como uma {@link Candlestick#LONG_LINE linha longa}
     * <li> NÃO SER UM {@link Candlestick#LONG_BLACK_CANDLE}
     * </ul>
     *
     * @see http://www.candlescanner.com/candlestick-patterns/long-black-candle/
     */
    public static final Predicate<List<OHLC>> BLACK_CANDLE = (rates) -> {
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░┌┴┐░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│▓│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│▓│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│▓│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░│▓│░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░└┬┘░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        // ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        return BLACK
                .and(ALL_SHADOW)
                .and(NO_SHADOW_LARGER_THAN_BODY)
                .and((t) -> LONG_LINE.test(rates))
                .and((e) -> LONG_CANDLE.negate().test(rates))
                .test(rates.get(0));
    };

    public static final Predicate<List<OHLC>> DOJI = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> DRAGONFLY_DOJI = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> GRAVESTONE_DOJI = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> LONG_LEGGED_DOJI = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> HANGING_MAN = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> HAMMER = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> INVERTED_BLACK_HAMMER = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> INVERTED_HAMMER = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> LONG_LOWER_SHADOW = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> LONG_UPPER_SHADOW = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> MARUBOZU = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> SHOOTING_STAR = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> SPINNING_TOP = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> WHITE_BODY = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> SHAVEN_BOTTOM = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> SHAVEN_HEAD = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> BEARISH_HARAMI = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> BEARISH_HARAMI_CROSS = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> BEARISH_3_METHOD_FORMATION = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> BULLISH_3_METHOD_FORMATION = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> BULLISH_HARAMI = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> BULLISH_HARAMI_CROSS = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> DARK_CLOUD_COVER = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> ENGULFING_BEARISH_LINE = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> ENGULFING_BULLISH = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> EVENING_DOJI_STAR = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> FALLING_WINDOW = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> MORNING_DOJI_STAR = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> MORNING_STAR = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> ON_NECKLINE = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> TWO_BLACK_GAPPING = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> THREE_BLACK_CROWS = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> THREE_WHITE_SOLDIERS = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> THREE_LINE_STRIKE = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> TWEEZER_BOTTOMS = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> TWEEZER_TOPS = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> DOJI_STAR = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> PIERCING_LINE = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> RISING_WINDOW = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> EVENING_STAR = (rates) -> {
        return true;
    };

    public static final Predicate<List<OHLC>> ABANDONED_BABY = (rates) -> {
        return true;
    };

    /**
     * Obtém o tamanho do candle
     * <p>
     * O tamanho do Candle é a diferença entre o maior e menor preço deste candle
     *
     * @param ohlc
     * @return
     */
    private static double size(final OHLC ohlc) {
        return ohlc.high - ohlc.low;
    }

    /**
     * Obtém o tamanho do corpo do candle
     *
     * @param ohlc
     * @return
     */
    private static double body(final OHLC ohlc) {
        return Math.abs(ohlc.open - ohlc.close);
    }

    /**
     * Obtém o tamanho médio dos corpos dos candles no periodo informado
     *
     * @param rates
     * @param period
     * @return
     */
    private static double averageBody(List<OHLC> rates, int period) {
        return cached("averageBody", rates, period, () -> {
            double sum = 0.0;
            for (int i = 0, j = rates.size(); i < j; i++) {
                final OHLC ohlc = rates.get(i);
                sum += body(ohlc);
            }

            return sum / Math.min(rates.size(), period);
        });
    }

    /**
     * Média exponencial dos tamanhos dos candles individuais para os período informado.
     *
     * <p>
     * Usado para determinar se um candle é Long ou Short
     *
     * @param rates
     * @param period
     * @return
     * @see Candlestick#size(info.alexrodin.ta.OHLC)
     * @see http://www.candlescanner.com/candlestick-patterns/long-and-short-lines/
     */
    private static double averageDistance(List<OHLC> rates, int period) {
        return cached("averageDistance", rates, period, () -> {
            double exponent = 2.0 / (period + 1);
            int start = Math.min(rates.size() - 1, period);
            OHLC ohlc = rates.get(start);

            // Primeiro registro (mais antigo), o EMA nao possui valores
            double ema = size(ohlc);

            for (int i = start - 1; i >= 0; i--) {
                ohlc = rates.get(i);
                ema = size(ohlc) * exponent + ema * (1 - exponent);
            }

            return ema;
        });
    }

    /**
     * Permite fazer o cache do processamento de uma lista. Evita processamento repetitivo no curto espaço de tempo
     *
     * @param method
     * @param rates
     * @param period
     * @return
     */
    private static double cached(
            String method,
            List<OHLC> rates,
            int period,
            Supplier<Double> exec
    ) {
        String key = method + "_" + rates.hashCode() + "_" + period;
        Double value = CACHE_RESULT.get(key);
        if (value == null) {
            value = exec.get();
            CACHE_RESULT.put(key, value);
        }
        return value;
    }
}