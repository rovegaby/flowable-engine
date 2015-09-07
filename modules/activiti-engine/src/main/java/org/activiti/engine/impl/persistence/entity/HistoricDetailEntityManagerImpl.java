/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.persistence.entity;

import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.impl.HistoricDetailQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricDetailDataManager;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class HistoricDetailEntityManagerImpl extends AbstractEntityManager<HistoricDetailEntity> implements HistoricDetailEntityManager {
  
  protected HistoricDetailDataManager historicDetailDataManager;
  
  public HistoricDetailEntityManagerImpl() {
    
  }
  
  public HistoricDetailEntityManagerImpl(HistoricDetailDataManager historicDetailDataManager) {
    this.historicDetailDataManager = historicDetailDataManager;
  }
  
  @Override
  protected DataManager<HistoricDetailEntity> getDataManager() {
    return historicDetailDataManager;
  }
  
  @Override
  public HistoricFormPropertyEntity insertHistoricFormPropertyEntity(ExecutionEntity execution, 
      String propertyId, String propertyValue, String taskId) {
    
    HistoricFormPropertyEntity historicFormPropertyEntity = new HistoricFormPropertyEntity();
    historicFormPropertyEntity.setProcessInstanceId(execution.getProcessInstanceId());
    historicFormPropertyEntity.setExecutionId(execution.getId());
    historicFormPropertyEntity.setTaskId(taskId);
    historicFormPropertyEntity.setPropertyId(propertyId);
    historicFormPropertyEntity.setPropertyValue(propertyValue);
    historicFormPropertyEntity.setTime(getClock().getCurrentTime());

    HistoricActivityInstanceEntity historicActivityInstance = getHistoryManager().findActivityInstance(execution, true, false);
    if (historicActivityInstance != null) {
      historicFormPropertyEntity.setActivityInstanceId(historicActivityInstance.getId());
    }
    
    insert(historicFormPropertyEntity);
    return historicFormPropertyEntity;
  }
  
  @Override
  public HistoricDetailVariableInstanceUpdateEntity copyAndInsertHistoricDetailVariableInstanceUpdateEntity(VariableInstanceEntity variableInstance) {
    HistoricDetailVariableInstanceUpdateEntity historicVariableUpdate = new HistoricDetailVariableInstanceUpdateEntity();
    historicVariableUpdate.processInstanceId = variableInstance.getProcessInstanceId();
    historicVariableUpdate.executionId = variableInstance.getExecutionId();
    historicVariableUpdate.taskId = variableInstance.getTaskId();
    historicVariableUpdate.time = getClock().getCurrentTime();
    historicVariableUpdate.revision = variableInstance.getRevision();
    historicVariableUpdate.name = variableInstance.getName();
    historicVariableUpdate.variableType = variableInstance.getType();
    historicVariableUpdate.textValue = variableInstance.getTextValue();
    historicVariableUpdate.textValue2 = variableInstance.getTextValue2();
    historicVariableUpdate.doubleValue = variableInstance.getDoubleValue();
    historicVariableUpdate.longValue = variableInstance.getLongValue();

    if (variableInstance.getBytes() != null) {
      String byteArrayName = "hist.detail.var-" + variableInstance.getName();
      historicVariableUpdate.byteArrayRef.setValue(byteArrayName, variableInstance.getBytes());
    }

    insert(historicVariableUpdate);
    return historicVariableUpdate;
  }
  
  @Override
  public void delete(HistoricDetailEntity entity, boolean fireDeleteEvent) {
    super.delete(entity, fireDeleteEvent);
    
    if (entity instanceof HistoricDetailVariableInstanceUpdateEntity) {
      ((HistoricDetailVariableInstanceUpdateEntity) entity).getByteArrayRef().delete();
    }
  }

  @Override
  public void deleteHistoricDetailsByProcessInstanceId(String historicProcessInstanceId) {
    if (getHistoryManager().isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      List<HistoricDetailEntity> historicDetails = historicDetailDataManager.findHistoricDetailsByProcessInstanceId(historicProcessInstanceId);

      for (HistoricDetailEntity historicDetail : historicDetails) {
        delete(historicDetail);
      }
    }
  }

  @Override
  public long findHistoricDetailCountByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery) {
    return historicDetailDataManager.findHistoricDetailCountByQueryCriteria(historicVariableUpdateQuery);
  }

  @Override
  public List<HistoricDetail> findHistoricDetailsByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery, Page page) {
    return historicDetailDataManager.findHistoricDetailsByQueryCriteria(historicVariableUpdateQuery, page);
  }

  @Override
  public void deleteHistoricDetailsByTaskId(String taskId) {
    if (getHistoryManager().isHistoryLevelAtLeast(HistoryLevel.FULL)) {
      List<HistoricDetailEntity> details = historicDetailDataManager.findHistoricDetailsByTaskId(taskId);
      for (HistoricDetail detail : details) {
        delete((HistoricDetailEntity) detail);
      }
    }
  }

  @Override
  public List<HistoricDetail> findHistoricDetailsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return historicDetailDataManager.findHistoricDetailsByNativeQuery(parameterMap, firstResult, maxResults);
  }

  @Override
  public long findHistoricDetailCountByNativeQuery(Map<String, Object> parameterMap) {
    return historicDetailDataManager.findHistoricDetailCountByNativeQuery(parameterMap);
  }

  public HistoricDetailDataManager getHistoricDetailDataManager() {
    return historicDetailDataManager;
  }

  public void setHistoricDetailDataManager(HistoricDetailDataManager historicDetailDataManager) {
    this.historicDetailDataManager = historicDetailDataManager;
  }
  
}