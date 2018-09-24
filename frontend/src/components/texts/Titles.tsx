import * as React from 'react';
import {Children} from '../../types/Types';
import {Row} from '../layouts/row/Row';
import {Bold, Normal, Small, Xlarge} from './Texts';
import './Titles.scss';

interface TitleProps {
  children: Children;
  className?: string;
  subtitle?: string;
}

export const PageTitle = ({children}: {children?: Children}) => (
  <Row className="space-between">
    <MainTitle>
      {children}
    </MainTitle>
  </Row>
);

export const MainTitle = ({children, subtitle}: TitleProps) => (
  <Row className="MainTitle">
    <Xlarge className="Bold">{children}</Xlarge>
    {subtitle && <Small className="Subtitle-description">{subtitle}</Small>}
  </Row>
);

export const Subtitle = ({children}: TitleProps) => (
  <Row className="Subtitle">
    <Bold>{children}</Bold>
  </Row>
);

export const AppTitle = ({children}: TitleProps) => (
  <Normal className="AppTitle">{children}</Normal>
);
