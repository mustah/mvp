import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {getEntitiesDomainModels, getError} from '../../../state/domain-models/domainModelsSelectors';
import {
  addOrganisation,
  addSubOrganisation,
  clearOrganisationErrors,
  fetchOrganisations,
  updateOrganisation
} from '../../../state/domain-models/organisation/organisationsApiActions';
import {getOrganisations} from '../../../state/domain-models/organisation/organisationSelectors';
import {clearUserSelectionErrors, fetchUserSelections} from '../../../state/user-selection/userSelectionActions';
import {DispatchToProps, OrganisationForm, StateToProps} from '../components/OrganisationForm';

const mapStateToProps = ({domainModels: {organisations, userSelections}}: RootState): StateToProps => ({
  isFetchingOrganisations: organisations.isFetching,
  isFetchingUserSelections: userSelections.isFetching,
  organisationsError: getError(organisations),
  userSelectionsError: getError(userSelections),
  organisations: getOrganisations(organisations),
  selections: getEntitiesDomainModels(userSelections),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addOrganisation,
  addSubOrganisation,
  updateOrganisation,
  fetchOrganisations,
  fetchUserSelections,
  clearOrganisationErrors,
  clearUserSelectionErrors,
}, dispatch);

export const OrganisationFormContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(OrganisationForm);
