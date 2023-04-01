/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
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
 *******************************************************************************/
package br.com.anteros.spring.transaction;

import javax.sql.DataSource;

import br.com.anteros.cloud.integration.filesharing.CloudFileManager;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.annotation.Converter;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.persistence.metadata.annotation.EnumValues;
import br.com.anteros.persistence.metadata.configuration.PersistenceModelConfiguration;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.configuration.AnterosPersistenceConfiguration;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuração Anteros usando uma fábrica criada para uso com sistema de transações do Spring.
 * 
 * @author Edson Martins edsonmartins2005@gmail.com
 *
 */
public class SpringSQLConfiguration extends AnterosPersistenceConfiguration {

	public SpringSQLConfiguration(DataSource dataSource, CloudFileManager externalFileManager, boolean enableImageCompression) {
		super(dataSource, externalFileManager,enableImageCompression);
	}

	public SpringSQLConfiguration(PersistenceModelConfiguration modelConfiguration, CloudFileManager externalFileManager, boolean enableImageCompression) {
		super(modelConfiguration, externalFileManager,enableImageCompression);
	}

	public SpringSQLConfiguration(DataSource dataSource, PersistenceModelConfiguration modelConfiguration, CloudFileManager externalFileManager, boolean enableImageCompression) {
		super(dataSource, modelConfiguration, externalFileManager,enableImageCompression);
	}

	@Override
	public SQLSessionFactory buildSessionFactory() throws Exception {
		prepareClassesToLoad();
		buildDataSource();		
		SpringSQLSessionFactoryImpl sessionFactory = new SpringSQLSessionFactoryImpl(entityCacheManager, dataSource,
				this.getSessionFactoryConfiguration(), this.externalFileManager, this.enableImageCompression);
		loadEntities(sessionFactory.getDialect());		
		sessionFactory.generateDDL();
		return sessionFactory;
	}

//	@Override
//	protected void prepareClassesToLoad() throws ClassNotFoundException {
//		LOG.debug("Preparando classes para ler entidades.");
//		if ((getSessionFactoryConfiguration().getPackageToScanEntity() != null)
//				&& (!"".equals(getSessionFactoryConfiguration().getPackageToScanEntity().getPackageName()))) {
//			if (getSessionFactoryConfiguration().isIncludeSecurityModel())
//				getSessionFactoryConfiguration().getPackageToScanEntity().setPackageName(
//						getSessionFactoryConfiguration().getPackageToScanEntity().getPackageName() + ", " + SECURITY_PACKAGE);
//			String[] _packages = StringUtils.tokenizeToStringArray(getSessionFactoryConfiguration().getPackageToScanEntity().getPackageName(), ", ;");
//			List<String> packages = new ArrayList<>();
//			for (String pk : _packages){
//				packages.add(StringUtils.replaceAll(pk,".*",""));
//			}
//			packages.add(CONVERTERS_PACKAGE);
//			List<Class<?>> scanClasses = this.findAllClassesInPackages(packages);
//			if (LOG.isDebugEnabled()) {
//				for (Class<?> cl : scanClasses) {
//					LOG.debug("Encontrado classe scaneada " + cl.getName());
//				}
//			}
//			getSessionFactoryConfiguration().addToAnnotatedClasses(scanClasses);
//		}
//
//		if ((getSessionFactoryConfiguration().getClasses() == null) || (getSessionFactoryConfiguration().getClasses().size() == 0))
//			LOG.debug("Não foram encontradas classes representando entidades. Informe o pacote onde elas podem ser localizadas ou informe manualmente cada uma delas.");
//
//		LOG.debug("Preparação das classes concluída.");
//	}

	protected List<Class<?>> findAllClassesInPackages(List<String> packages) {
		final List<Class<?>> result = new LinkedList<Class<?>>();
		final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
				false, new StandardServletEnvironment());
		provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		provider.addIncludeFilter(new AnnotationTypeFilter(EnumValues.class));
		provider.addIncludeFilter(new AnnotationTypeFilter(Converter.class));
		for (String packageName : packages) {
			for (BeanDefinition beanDefinition : provider
					.findCandidateComponents(packageName)) {
				try {
					result.add(Class.forName(beanDefinition.getBeanClassName()));
				} catch (ClassNotFoundException e) {
					LOG.warn(
							"Não foi possível resolver o objeto de classe para definição de bean", e);
				}
			}
		}
		return result;
	}

}
