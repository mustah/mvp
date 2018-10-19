import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
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
  fetchOrganisations
} from '../../../state/domain-models/organisation/organisationsApiActions';
import {getOrganisations} from '../../../state/domain-models/organisation/organisationSelectors';
import {CallbackOfData, CallbackOfDataAndUrlParameters, ClearError, ErrorResponse, Fetch} from '../../../types/Types';

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
}

type OwnProps = InjectedAuthRouterProps;
type Props = OwnProps & StateToProps & DispatchToProps;

class OrganisationEdit extends React.Component<Props, {}> {

  componentDidMount() {
    this.props.fetchOrganisations();
  }

  componentWillReceiveProps({fetchOrganisations}: Props) {
    fetchOrganisations();
  }

  render() {
    const {addOrganisation, addSubOrganisation, organisations, isFetching, error, clearError} = this.props;
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
  fetchOrganisations,
  clearError: clearOrganisationErrors,
}, dispatch);

export const OrganisationEditContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(OrganisationEdit);
