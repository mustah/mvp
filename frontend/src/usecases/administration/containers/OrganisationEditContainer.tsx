import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {paperStyle} from '../../../app/themes';
import {OrganisationEditForm} from '../../../components/forms/OrganisationEditForm';
import {WrapperIndent} from '../../../components/layouts/wrapper/Wrapper';
import {Loader} from '../../../components/loading/Loader';
import {PageTitle} from '../../../components/texts/Titles';
import {AdminPageComponent} from '../../../containers/PageComponent';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {
  addOrganisation,
  addSubOrganisation,
  clearOrganisationErrors,
  fetchOrganisations,
  updateOrganisation
} from '../../../state/domain-models/organisation/organisationsApiActions';
import {getOrganisations} from '../../../state/domain-models/organisation/organisationSelectors';
import {
  CallbackOfData,
  CallbackOfDataAndUrlParameters,
  ClearError,
  ErrorResponse,
  Fetch,
  uuid
} from '../../../types/Types';

const organisationByIdIfExisting = (organisationId: uuid, organisations: Organisation[]): Organisation | undefined =>
  organisations.find(({id}) => id === organisationId);

interface StateToProps {
  organisations: Organisation[];
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  addOrganisation: CallbackOfData;
  addSubOrganisation: CallbackOfDataAndUrlParameters;
  fetchOrganisations: Fetch;
  clearError: ClearError;
  updateOrganisation: CallbackOfData;
}

type OwnProps = InjectedAuthRouterProps & RouteComponentProps<{organisationId: uuid}>;
type Props = OwnProps & StateToProps & DispatchToProps;

class OrganisationEdit extends React.Component<Props, {}> {

  componentDidMount() {
    this.props.fetchOrganisations();
  }

  componentWillReceiveProps({fetchOrganisations}: Props) {
    fetchOrganisations();
  }

  render() {
    const {
      addOrganisation,
      addSubOrganisation,
      organisations,
      isFetching,
      error,
      clearError,
      match: {params: {organisationId}},
      updateOrganisation,
    } = this.props;

    return (
      <AdminPageComponent>
        <PageTitle>
          {translate('add organisation')}
        </PageTitle>

        <Paper style={paperStyle}>
          <WrapperIndent>
            <Loader isFetching={isFetching} error={error} clearError={clearError}>
              <OrganisationEditForm
                addOrganisation={addOrganisation}
                addSubOrganisation={addSubOrganisation}
                organisations={organisations}
                organisation={organisationByIdIfExisting(organisationId, organisations)}
                updateOrganisation={updateOrganisation}
              />
            </Loader>
          </WrapperIndent>
        </Paper>
      </AdminPageComponent>
    );
  }
}

const mapStateToProps = ({auth, domainModels: {organisations}}: RootState): StateToProps => ({
  isFetching: organisations.isFetching,
  error: getError(organisations),
  organisations: getOrganisations(organisations),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addOrganisation,
  addSubOrganisation,
  updateOrganisation,
  fetchOrganisations,
  clearError: clearOrganisationErrors,
}, dispatch);

export const OrganisationEditContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(OrganisationEdit);
