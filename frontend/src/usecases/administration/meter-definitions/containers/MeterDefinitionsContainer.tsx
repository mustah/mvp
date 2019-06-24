import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withCssStyles} from '../../../../components/hoc/withThemeProvider';
import {RootState} from '../../../../reducers/rootReducer';
import {getError} from '../../../../state/domain-models/domainModelsSelectors';
import {
  clearMeterDefinitionErrors,
  deleteMeterDefinition,
  fetchMeterDefinitions
} from '../../../../state/domain-models/meter-definitions/meterDefinitionsApiActions';
import {DispatchToProps, MeterDefinitionList, StateToProps} from '../components/MeterDefinitionList';

const mapStateToProps = ({domainModels: {meterDefinitions}}: RootState): StateToProps => ({
  error: getError(meterDefinitions),
  isFetching: meterDefinitions.isFetching,
  meterDefinitions,
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: clearMeterDefinitionErrors,
  deleteMeterDefinition,
  fetchMeterDefinitions,
}, dispatch);

export const MeterDefinitionsContainer =
  connect(mapStateToProps, mapDispatchToProps)(withCssStyles(MeterDefinitionList));
