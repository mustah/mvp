import Card from 'material-ui/Card/Card';
import * as React from 'react';
import {Column} from '../../../../components/layouts/column/Column';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {Subtitle} from '../../../../components/texts/Titles';
import {Children} from '../../../../types/Types';
import './Widget.scss';

interface Props {
  title?: string;
  children: Children;
}

export const Widget = ({title, children}: Props) => (
  <Card className="Widget">
    <RowMiddle className="space-between">
      <Subtitle className="Widget-subtitle">{title}</Subtitle>
    </RowMiddle>
    <Column className="Content">
      {children}
    </Column>
  </Card>
);
