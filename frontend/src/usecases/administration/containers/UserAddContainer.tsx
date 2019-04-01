import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../app/themes';
import {UserEditForm} from '../../../components/forms/UserEditForm';
import {AdminPageLayout} from '../../../components/layouts/layout/PageLayout';
import {RowIndented} from '../../../components/layouts/row/Row';
import {RetryLoader} from '../../../components/loading/Loader';
import {MainTitle} from '../../../components/texts/Titles';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {
  clearOrganisationErrors,
  fetchOrganisations,
} from '../../../state/domain-models/organisation/organisationsApiActions';
import {getOrganisations} from '../../../state/domain-models/organisation/organisationSelectors';
import {addUser} from '../../../state/domain-models/user/userApiActions';
import {Role} from '../../../state/domain-models/user/userModels';
import {getRoles} from '../../../state/domain-models/user/userSelectors';
import {Language} from '../../../state/language/languageModels';
import {getLanguages} from '../../../state/language/languageSelectors';
import {ErrorResponse, Fetch, OnClick, OnClickEventHandler} from '../../../types/Types';
import {getUser} from '../../auth/authSelectors';

interface StateToProps {
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
  organisations: Organisation[];
  roles: Role[];
  languages: Language[];
}

interface DispatchToProps {
  addUser: OnClickEventHandler;
  fetchOrganisations: Fetch;
  clearError: OnClick;
}

type Props = DispatchToProps & StateToProps;

class UserAdd extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchOrganisations();
  }

  componentWillReceiveProps({fetchOrganisations}: Props) {
    fetchOrganisations();
  }

  render() {
    const {addUser, clearError, isFetching, error, organisations, roles, languages} = this.props;
    return (
      <AdminPageLayout>
        <MainTitle>{translate('add user')}</MainTitle>

        <Paper style={paperStyle}>
          <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
            <RowIndented>
              <UserEditForm
                organisations={organisations}
                onSubmit={addUser}
                possibleRoles={roles}
                isEditSelf={false}
                languages={languages}
              />
            </RowIndented>
          </RetryLoader>
        </Paper>
      </AdminPageLayout>
    );
  }
}

const mapStateToProps = ({domainModels: {organisations}, auth}: RootState): StateToProps => ({
  organisations: getOrganisations(organisations),
  roles: getRoles(getUser(auth)),
  languages: getLanguages(),
  isFetching: organisations.isFetching,
  error: getError(organisations),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addUser,
  fetchOrganisations,
  clearError: clearOrganisationErrors,
}, dispatch);

export const UserAddContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(UserAdd);
