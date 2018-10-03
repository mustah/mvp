import {default as classNames} from 'classnames';
import * as React from 'react';
import {componentOrNull} from '../../components/hoc/hocs';
import {superAdminOnly} from '../../components/hoc/withRoles';
import {Column} from '../../components/layouts/column/Column';
import {Normal} from '../../components/texts/Texts';
import {ClassNamed, WithChildren} from '../../types/Types';
import './Info.scss';

interface InfoProps extends ClassNamed, WithChildren {
  label: string;
}

const InfoComponent = ({children, className, label}: InfoProps) => (
  <Column className={classNames('Info', className)}>
    <Normal className="Info-label">{label}</Normal>
    {children}
  </Column>
);

export const Info = componentOrNull<InfoProps>(({children}: InfoProps) => !!children)(InfoComponent);

export const SuperAdminInfo = superAdminOnly<InfoProps>(Info);
