package ru.ts.toykernel.plugins.facils.utils;

import ru.ts.utils.data.Pair;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 09.12.2008
 * Time: 18:19:16
 * To change this template use File | Settings | File Templates.
 */
public interface IErrorHandler
{
    int onError(Pair<String, Integer> message_code) throws Exception;
}
