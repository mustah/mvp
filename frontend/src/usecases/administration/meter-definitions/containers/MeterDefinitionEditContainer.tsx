import {connect} from 'react-redux';
import {bindActionCreators, Dispatch} from 'redux';
import {isDefined} from '../../../../helpers/commonHelpers';
import {Maybe} from '../../../../helpers/Maybe';
import {RootState} from '../../../../reducers/rootReducer';
import {getEntitiesDomainModels} from '../../../../state/domain-models/domainModelsSelectors';
import {fetchMediums} from '../../../../state/domain-models/medium/mediumModels';
import {
  addMeterDefinition,
  fetchMeterDefinitions,
  updateMeterDefinition
} from '../../../../state/domain-models/meter-definitions/meterDefinitionsApiActions';
import {fetchOrganisations} from '../../../../state/domain-models/organisation/organisationsApiActions';
import {getOrganisations} from '../../../../state/domain-models/organisation/organisationSelectors';
import {fetchQuantities} from '../../../../state/domain-models/quantities/quantitesApiActions';
import {DispatchToProps, MeterDefinitionEdit, OwnProps, StateToProps} from '../components/MeterDefinitionEdit';

const mapStateToProps = ({
  domainModels: {organisations, mediums, meterDefinitions, quantities}
}: RootState): StateToProps => ({
  error: Maybe.maybe([organisations.error, mediums.error, quantities.error, meterDefinitions.error].find(isDefined)),
  isFetching: organisations.isFetching || meterDefinitions.isFetching || mediums.isFetching || quantities.isFetching,
  organisations: getOrganisations(organisations),
  meterDefinitions: getEntitiesDomainModels(meterDefinitions),
  mediums: getEntitiesDomainModels(mediums),
  quantities: getEntitiesDomainModels(quantities),
});

const mapDispatchToProps = (dispatch: Dispatch): DispatchToProps => bindActionCreators({
  addMeterDefinition,
  updateMeterDefinition,
  fetchMeterDefinitions,
  fetchOrganisations,
  fetchMediums,
  fetchQuantities
}, dispatch);

export const MeterDefinitionEditContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterDefinitionEdit);
