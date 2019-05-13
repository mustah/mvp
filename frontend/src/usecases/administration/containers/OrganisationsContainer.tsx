import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withCssStyles} from '../../../components/hoc/withThemeProvider';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {
  clearOrganisationErrors,
  deleteOrganisation,
  fetchOrganisations,
} from '../../../state/domain-models/organisation/organisationsApiActions';
import {ClearError, ErrorResponse, Fetch, OnClickWithId} from '../../../types/Types';
import {OrganisationList} from '../components/OrganisationList';

export interface StateToProps {
  organisations: DomainModel<Organisation>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

export interface DispatchToProps {
  deleteOrganisation: OnClickWithId;
  fetchOrganisations: Fetch;
  clearError: ClearError;
}

const mapStateToProps = ({domainModels: {organisations}}: RootState): StateToProps => ({
  organisations: getDomainModel(organisations),
  isFetching: organisations.isFetching,
  error: getError(organisations),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteOrganisation,
  fetchOrganisations,
  clearError: clearOrganisationErrors,
}, dispatch);

export const OrganisationsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(withCssStyles(OrganisationList));
