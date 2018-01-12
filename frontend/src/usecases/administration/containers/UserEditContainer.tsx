import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../app/themes';
import {Row} from '../../../components/layouts/row/Row';
import {WrapperIndent} from '../../../components/layouts/wrapper/Wrapper';
import {MainTitle} from '../../../components/texts/Titles';
import {PageComponent} from '../../../containers/PageComponent';
import {translate} from '../../../services/translationService';
import {addUser} from '../../../state/domain-models/domainModelsActions';
import {Organisation, Role} from '../../../state/domain-models/user/userModels';
import {OnClick} from '../../../types/Types';
import {UserEditForm} from './UserEditForm';

interface StateToProps {
  organisations: Organisation[];
  roles: Role[];
}

interface DispatchToProps {
  addUser: OnClick;
}

const UserEditContainerComponent = (props: DispatchToProps & StateToProps) => {
  const {addUser, organisations, roles} = props;
  return (
    <PageComponent isSideMenuOpen={false}>
      <Row className="space-between">
        <MainTitle>
          {translate('add user')}
        </MainTitle>
      </Row>

      <Paper style={paperStyle}>
        <WrapperIndent>
          <UserEditForm
            organisations={organisations}
            onSubmit={addUser}
            roles={roles}
          />
        </WrapperIndent>
      </Paper>
    </PageComponent>
  );
};

// TODO get organisations and roles from backend
const mapStateToProps = (): StateToProps => ({
  organisations: [
    {id: 1, code: 'elvaco', name: 'Elvaco'},
    {id: 2, code: 'wayne-industries', name: 'Wayne Industries'},
  ],
  roles: [
    Role.ADMIN,
    Role.USER,
  ],
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addUser,
}, dispatch);

export const UserEditContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(UserEditContainerComponent);
