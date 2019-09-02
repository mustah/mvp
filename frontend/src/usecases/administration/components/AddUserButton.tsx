import * as React from 'react';
import '../../../components/actions-dropdown/ActionsDropdown.scss';
import {ButtonAdd} from '../../../components/buttons/ButtonAdd';
import {Link} from '../../../components/links/Link';
import {translate} from '../../../services/translationService';

interface Props {
  linkTo: string;
}

export const AddUserButton = ({linkTo}: Props) => (
  <Link to={linkTo} key={'add user'}>
    <ButtonAdd label={translate('add user')}/>
  </Link>
);
