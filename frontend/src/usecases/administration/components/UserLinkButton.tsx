import * as classNames from 'classnames';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {ButtonLink} from '../../../components/buttons/ButtonLink';
import {ClassNamed} from '../../../types/Types';
import 'UserLinkButton.scss';

interface UserLinkButtonProps extends ClassNamed {
  to: string;
  text: string;
}

export const UserLinkButton = ({to, text, className}: UserLinkButtonProps) => (
  <Link to={to} className={classNames('link', 'UserLinkButton', className)}>
    <ButtonLink className="Link-margin">{text}</ButtonLink>
  </Link>
);
