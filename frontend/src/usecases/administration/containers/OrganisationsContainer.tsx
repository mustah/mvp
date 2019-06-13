import {toArray} from 'lodash';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withCssStyles} from '../../../components/hoc/withThemeProvider';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {
  clearOrganisationErrors,
  deleteOrganisation,
  fetchOrganisations,
} from '../../../state/domain-models/organisation/organisationsApiActions';
import {ClearError, ErrorResponse, Fetch, OnClickWithId} from '../../../types/Types';
import {syncMetersOrganisation} from '../../meter/meterActions';
import {OrganisationList} from '../components/OrganisationList';

export interface StateToProps {
  error: Maybe<ErrorResponse>;
  isFetching: boolean;
  organisations: Organisation[];
}

export interface DispatchToProps {
  clearError: ClearError;
  deleteOrganisation: OnClickWithId;
  fetchOrganisations: Fetch;
  syncMetersOrganisation: OnClickWithId;
}

const mapStateToProps = ({domainModels: {organisations}}: RootState): StateToProps => ({
  error: getError(organisations),
  isFetching: organisations.isFetching,
  organisations: toArray(organisations.entities),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: clearOrganisationErrors,
  deleteOrganisation,
  fetchOrganisations,
  syncMetersOrganisation,
}, dispatch);

export const OrganisationsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(withCssStyles(OrganisationList));
