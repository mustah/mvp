import {default as classNames} from 'classnames';
import * as React from 'react';
import {ColumnCenter} from '../../../components/layouts/column/Column';
import {ClassNamed, Titled, WithChildren} from '../../../types/Types';
import {MenuUnderline} from './MenuUnderline';

type Props = ClassNamed & Titled & WithChildren;

export const TopMenuItem = ({children, className, title}: Props) => (
  <ColumnCenter className={classNames('TopMenu-Item', className)} title={title}>
    {children}
    <MenuUnderline/>
  </ColumnCenter>
);
