import * as React from 'react';

interface LogoProps {
  fill: string;
}

const eonSvg = 'M340.669,56.204c0,15.368-9.576,35.559-16.497,48.9 ' +
  ' c-10.87,20.991-25.576,33.57-49.488,35.054c-21.136,1.311-68.905,' +
  '4.98-60.536-30.635c3.629-15.419,11.693-29.004,16.644-43.893 ' +
  ' c16.636-33.192,58.501-39.52,91.76-31.225C333.81,38.501,339.803,43.982,340.669,56.204L340.669,56.204z ' +
  ' M284.846,57.677 ' +
  ' c-17.096,0-19.178,23.863-22.387,36.675c-0.807,4.841-6.228,21.062,' +
  '2.504,21.062c20.555,4.135,22.466-18.41,25.922-33.434 ' +
  ' c1.271-5.527,3.792-14.8,2.652-20.474C293.537,57.286,287.652,57.959,284.846,57.677L284.846,57.677z ' +
  ' M167.163,58.708 ' +
  ' c-2.109,40.416-30.497,37.271-64.218,37.116c-12.815-0.053-25.921-' +
  '0.884-38.59-0.884c-4.675,4.742,8.859,19.123,12.667,21.504 ' +
  ' c12.997,8.097,29.305,4.621,39.768-5.892c5.621,0,65.955-7.886,' +
  '45.07,12.962c-10.108,8.993-24.409,14.306-37.412,17.38 ' +
  ' c-41.372,9.784-130.739-14-100.597-72.319C44.456,34.663,89.085,' +
  '16.12,125.922,6.568c3.43-0.686,6.819-1.214,10.311-1.473 ' +
  ' C162.441,3.138,167.163,39.309,167.163,58.708L167.163,58.708z ' +
  ' M128.721,55.468c0-3.656,0.15-7.521-1.031-11.047 ' +
  ' c-4.045-16.297-46.489,18.505-51.55,23.566c-0.636,1.28-0.155,0.883,0.736,1.326c14.086,0,28.205-0.147,42.272-0.147 ' +
  ' C128.411,67.636,128.721,64.817,128.721,55.468L128.721,55.468z ' +
  ' M210.466,76.236c-3.193,6.432-2.48,14.868-8.985,19.736 ' +
  ' c-8.896,6.655-31.303,6.674-26.364-9.427c4.676-15.306,13.516-21.815,' +
  '29.605-17.822C208.646,70.038,209.573,72.637,210.466,76.236 ' +
  ' L210.466,76.236z ' +
  ' M490.607,56.351c0,4.121-0.864,8.369-1.768,12.373c-5.316,23.5-13.89,45.859-21.504,68.489 ' +
  ' c-6.986,6.986-21.51,3.182-29.016-1.325c-3.755-3.755,5.578-22.913,' +
  '7.069-27.544c2.288-7.11,14.785-33.332,4.271-38.588 ' +
  ' c-3.382,0-6.071,0.443-9.426,1.326c-31.462,11.015-53.794,' +
  '43.041-76.442,65.69c-9.817,4.943-16.867,3.678-26.512-1.178 ' +
  ' c-0.957-0.957-0.494-2.479-0.296-3.684c7.299-30.482,' +
  '18.842-60.707,31.373-89.403c1.972-5.859,22.229-3.688,27.249-1.178 ' +
  ' c0,6.489-0.682,17.38,8.689,17.38c21.023-2.317,72.536-47.952,' +
  '85.574-8.837C490.24,52.079,490.607,54.081,490.607,56.351 ' +
  ' L490.607,56.351z';

export const Logo = (props: LogoProps) => {
  const {fill} = props;
  return (
    <svg
      version="1.1"
      id="svg2002"
      image-rendering="optimizeQuality"
      text-rendering="geometricPrecision"
      shape-rendering="geometricPrecision"
      x="0px"
      y="0px"
      width="200px"
      height="148.344px"
      viewBox="0 0 508.246 148.344"
      enable-background="new 0 0 508.246 148.344"
    >
      <g transform="translate(-6.041939e-2,0.147204)">
        <path
          fill={fill}
          d={eonSvg}
        />
      </g>
    </svg>
  );
};
