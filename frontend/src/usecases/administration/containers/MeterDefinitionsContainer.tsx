import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {MeterDefinition} from '../../../state/domain-models/meter-definitions/meterDefinitionModels';
import {
  clearMeterDefinitionErrors,
  deleteMeterDefinition, fetchMeterDefinitions
} from '../../../state/domain-models/meter-definitions/meterDefinitionsApiActions';

import {ClearError, ErrorResponse, Fetch, OnClickWithId} from '../../../types/Types';
import {MeterDefinitionList} from '../components/MeterDefinitionList';

export interface StateToProps {
  meterDefinitions: DomainModel<MeterDefinition>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

export interface DispatchToProps {
  deleteMeterDefinition: OnClickWithId;
  fetchMeterDefinitions: Fetch;
  clearError: ClearError;
}

const mapStateToProps = ({domainModels: {meterDefinitions}}: RootState): StateToProps => ({
  meterDefinitions: getDomainModel(meterDefinitions),
  isFetching: meterDefinitions.isFetching,
  error: getError(meterDefinitions),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteMeterDefinition,
  fetchMeterDefinitions,
  clearError: clearMeterDefinitionErrors,
}, dispatch);

export const MeterDefinitionsContainer = connect<StateToProps, DispatchToProps>(
  mapStateToProps,
  mapDispatchToProps,
)(MeterDefinitionList);
