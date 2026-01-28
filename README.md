<h1>MarketLens</h1>

<p>Link: https://marketinvestment-analytics.netlify.app/</p>

<p>
  <strong>MarketLens</strong> is a full-stack AI-powered market analysis platform that provides insights into
  financial markets using machine learning, data pipelines, and a modern web interface. The project consists of a
  React frontend and a Java backend, built and deployed as separate services.
</p>

<hr />

<h2>🚀 Features</h2>
<ul>
  <li>📊 Interactive dashboard for market insights</li>
  <li>🤖 AI-generated analysis using automated pipelines</li>
  <li>⚡ Fast, responsive React frontend</li>
  <li>🔐 Clean separation of frontend and backend</li>
  <li>☁️ Deployment-ready structure</li>
</ul>

<hr />

<h2>🧱 Project Structure</h2>
<pre><code>
marketlens/
├── frontend/                 # React application
│   ├── src/
│   ├── public/
│   ├── yarn.lock
│   └── package.json
│
├── backend-java/              # Java backend (Spring Boot)
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
│
├── .gitignore
└── README.md
</code></pre>

<hr />

<h2>🛠 Tech Stack</h2>

<strong>Frontend:</strong>
<ul>
  <li>React (Create React App)</li>
  <li>JavaScript</li>
  <li>CSS</li>
  <li>Yarn</li>
</ul>

<strong>Backend:</strong>
<ul>
  <li>Java</li>
  <li>Spring Boot</li>
  <li>Maven</li>
</ul>

<hr />

<h2>⚙️ Local Development</h2>

<p><strong>Frontend</strong></p>
<pre><code>cd frontend
yarn install
yarn start
</code></pre>

<p>Runs at <code>http://localhost:3000</code></p>

<p><strong>Backend</strong></p>
<pre><code>cd backend-java
mvn spring-boot:run
</code></pre>

<p>Runs at <code>http://localhost:8080</code></p>

<hr />

<h2>🏗 Build</h2>

<p><strong>Frontend</strong></p>
<pre><code>yarn build
</code></pre>

<p><strong>Backend</strong></p>
<pre><code>mvn clean package
</code></pre>

<hr />

<h2>🌐 Deployment</h2>

<ul>
  <li><strong>Frontend:</strong> Netlify / Vercel</li>
  <li><strong>Backend:</strong> Render / Railway / Fly.io</li>
</ul>

<p>
  Build artifacts (<code>build/</code>, <code>target/</code>, <code>node_modules/</code>) are ignored by git.
</p>

<hr />

<h2>🔒 Environment Variables</h2>
<pre><code>REACT_APP_API_URL=http://localhost:8080
</code></pre>

<hr />

<h2>👩‍💻 Author</h2>
<p>
  <strong>Rithika Annareddy</strong><br />
  Data Engineering &amp; Analytics | AI &amp; ML<br />
  The Ohio State University
</p>
