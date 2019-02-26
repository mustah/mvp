import {values} from 'lodash';
import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {paperStyle} from '../../../app/themes';
import {OrganisationEditForm} from '../../../components/forms/OrganisationEditForm';
import {RowIndented} from '../../../components/layouts/row/Row';
import {RetryLoader} from '../../../components/loading/Loader';
import {MainTitle} from '../../../components/texts/Titles';
import {AdminPageLayout} from '../../../containers/PageLayout';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getEntitiesDomainModels, getError} from '../../../state/domain-models/domainModelsSelectors';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {
  addOrganisation,
  addSubOrganisation,
  clearOrganisationErrors,
  fetchOrganisations,
  updateOrganisation
} from '../../../state/domain-models/organisation/organisationsApiActions';
import {getOrganisations} from '../../../state/domain-models/organisation/organisationSelectors';
import {clearUserSelectionErrors, fetchUserSelections} from '../../../state/user-selection/userSelectionActions';
import {UserSelection} from '../../../state/user-selection/userSelectionModels';
import {
  CallbackWithData,
  CallbackWithDataAndUrlParameters,
  ClearError,
  ErrorResponse,
  Fetch,
  uuid
} from '../../../types/Types';

const organisationByIdIfExisting = (organisationId: uuid, organisations: Organisation[]): Organisation | undefined =>
  organisations.find(({id}) => id === organisationId);

interface StateToProps {
  organisations: Organisation[];
  isFetchingOrganisations: boolean;
  isFetchingUserSelections: boolean;
  organisationsError: Maybe<ErrorResponse>;
  userSelectionsError: Maybe<ErrorResponse>;
  selections: ObjectsById<UserSelection>;
}

interface DispatchToProps {
  addOrganisation: CallbackWithData;
  addSubOrganisation: CallbackWithDataAndUrlParameters;
  fetchOrganisations: Fetch;
  fetchUserSelections: Fetch;
  clearOrganisationErrors: ClearError;
  clearUserSelectionErrors: ClearError;
  updateOrganisation: CallbackWithData;
}

type OwnProps = InjectedAuthRouterProps & RouteComponentProps<{organisationId: uuid}>;
type Props = OwnProps & StateToProps & DispatchToProps;

class OrganisationEdit extends React.Component<Props, {}> {

  componentDidMount() {
    this.props.fetchOrganisations();
    this.props.fetchUserSelections();
  }

  componentWillReceiveProps({fetchOrganisations, fetchUserSelections}: Props) {
    fetchOrganisations();
    fetchUserSelections();
  }

  render() {
    const {
      addOrganisation,
      addSubOrganisation,
      organisations,
      isFetchingOrganisations,
      isFetchingUserSelections,
      organisationsError,
      userSelectionsError,
      clearOrganisationErrors,
      clearUserSelectionErrors,
      match: {params: {organisationId}},
      updateOrganisation,
      selections
    } = this.props;

    const title: string =
      organisationId
        ? translate('edit organisation')
        : translate('add organisation');

    return (
      <AdminPageLayout>
        <MainTitle>{title}</MainTitle>

        <Paper style={paperStyle}>
          <RowIndented>
            <RetryLoader
              isFetching={isFetchingOrganisations}
              error={organisationsError}
              clearError={clearOrganisationErrors}
            >
              <RetryLoader
                isFetching={isFetchingUserSelections}
                error={userSelectionsError}
                clearError={clearUserSelectionErrors}
              >
                <OrganisationEditForm
                  addOrganisation={addOrganisation}
                  addSubOrganisation={addSubOrganisation}
                  organisations={organisations}
                  organisation={organisationByIdIfExisting(organisationId, organisations)}
                  updateOrganisation={updateOrganisation}
                  selections={values(selections)}
                />
              </RetryLoader>
            </RetryLoader>
          </RowIndented>
        </Paper>
      </AdminPageLayout>
    );
  }
}

const mapStateToProps = ({auth, domainModels: {organisations, userSelections}}: RootState): StateToProps => ({
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

export const OrganisationEditContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(OrganisationEdit);
