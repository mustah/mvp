import * as classNames from 'classnames';
import Card from 'material-ui/Card/Card';
import * as React from 'react';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {Subtitle} from '../../../../components/texts/Titles';
import {Children, ClassNamed} from '../../../../types/Types';
import './Widget.scss';

interface Props extends ClassNamed {
  children: Children;
}

export const Widget = ({children, className}: Props) => (
  <Card className={classNames('Widget', className)}>
    {children}
  </Card>
);

interface WidgetWithTitleProps extends Props {
  title: string;
}

export const WidgetWithTitle = ({title, children, className}: WidgetWithTitleProps) => (
  <Widget className={className}>
    <RowMiddle className="space-between">
      <Subtitle className="Widget-subtitle">{title}</Subtitle>
    </RowMiddle>
    {children}
  </Widget>
);
