/**
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver;

import org.ballerinalang.model.Name;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Package context to keep the builtin and the current package.
 */
public class BLangPackageContext {
    private BLangPackage builtin;

    private BLangPackage current;

    private Map<String, BLangPackage> packageMap = new HashMap<>();

    public BLangPackageContext(BLangPackage builtin, BLangPackage current) {
        this.builtin = builtin;
        this.current = current;
        this.packageMap.put(getPackageName(((BLangPackageDeclaration) builtin.getPackageDeclaration()).pkgNameComps)
                , builtin);
        this.packageMap.put(getPackageName(((BLangPackageDeclaration) current.getPackageDeclaration()).pkgNameComps)
                , current);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getItems(Class type) {
        if (type.equals(BLangFunction.class)) {
            List<BLangFunction> functions = new ArrayList<>();
            functions.addAll((builtin.getFunctions()));
            functions.addAll(current.getFunctions());
            return (List<T>) functions;
        }
        return null;
    }

    /**
     * Get package by name.
     *
     * @param compilerContext compiler context
     * @param name            name of the package
     * @return ballerina lang package
     */
    public BLangPackage getPackageByName(CompilerContext compilerContext, Name name) {
        if (isPackageAvailable(name.getValue())) {
            return packageMap.get(name.getValue());
        } else {
            BLangPackage bLangPackage =
                    BallerinaPackageLoader.getPackageByName(compilerContext, name);
            addPackage(bLangPackage);
            return bLangPackage;
        }
    }

    /**
     * check whether the package is available or not.
     *
     * @param name name of the package
     * @return true if the package exist else false
     */
    private boolean isPackageAvailable(String name) {
        return packageMap.get(name) != null;
    }

    /**
     * get the package name by composing from given identifier list.
     *
     * @param compos list of path identifiers
     * @return string package name
     */
    private String getPackageName(List<BLangIdentifier> compos) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < compos.size(); i++) {
            stringBuilder.append(compos.get(i).getValue());
            if ((i + 1) != compos.size()) {
                stringBuilder.append('.');
            }
        }

        return stringBuilder.toString();
    }

    /**
     * add package to the package map.
     *
     * @param bLangPackage ballerina package to be added.
     */
    private void addPackage(BLangPackage bLangPackage) {
        this.packageMap
                .put(getPackageName(((BLangPackageDeclaration) bLangPackage.getPackageDeclaration()).pkgNameComps)
                        , bLangPackage);
    }
}