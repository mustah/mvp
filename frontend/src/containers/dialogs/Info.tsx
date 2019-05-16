import {default as classNames} from 'classnames';
import * as React from 'react';
import {compose} from 'recompose';
import {componentOrNothing} from '../../components/hoc/hocs';
import {withSuperAdminOnly} from '../../components/hoc/withRoles';
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

const withChildren = componentOrNothing<InfoProps>(({children}) => !!children);

export const Info = withChildren(InfoComponent);

export const SuperAdminInfo = compose<InfoProps, InfoProps>(withSuperAdminOnly, withChildren)(InfoComponent);
