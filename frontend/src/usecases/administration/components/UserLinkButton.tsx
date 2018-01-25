import * as classNames from 'classnames';
import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {Link} from 'react-router-dom';
import 'UserLinkButton.scss';
import {ClassNamed} from '../../../types/Types';

interface UserLinkButtonProps extends ClassNamed {
  to: string;
  text: string;
}

export const UserLinkButton = ({to, text, className}: UserLinkButtonProps) => (
  <Link to={to} className={classNames('UserLinkButton', className)}>
    <FlatButton className="Button" label={text}/>
  </Link>
);
