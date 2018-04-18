import * as classNames from 'classnames';
import Card from 'material-ui/Card/Card';
import * as React from 'react';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {Subtitle} from '../../../../components/texts/Titles';
import {Children, ClassNamed} from '../../../../types/Types';
import './Widget.scss';

interface Props extends ClassNamed {
  title?: string;
  children: Children;
}

export const Widget = ({title, children, className}: Props) => (
  <Card className={classNames('Widget', className)}>
    <RowMiddle className="space-between">
      <Subtitle className="Widget-subtitle">{title}</Subtitle>
    </RowMiddle>
    {children}
  </Card>
);
