const jsonServer = require('json-server');

const server = jsonServer.create();
const router = jsonServer.router('db.json');
const middlewares = jsonServer.defaults();

server.use(middlewares);
server.use(jsonServer.bodyParser);

// 회원가입
server.post('/signup', (req, res) => {
  const { userId, password, nickname } = req.body;
  const users = router.db.get('users').value();
  if (users.find(u => u.userId === userId)) {
    return res.status(400).json({
      success: false,
      message: '회원가입에 실패했습니다.',
      errors: { username: '이미 사용중인 아이디입니다.' }
    });
  }
  const id = users.length + 1;
  router.db.get('users').push({ id, userId, password, nickname }).write();
  res.status(201).json({
    success: true,
    message: '회원가입이 완료되었습니다.',
    username: id
  });
});

// 로그인
server.post('/login', (req, res) => {
  const { userId, password } = req.body;
  const user = router.db.get('users').find({ userId, password }).value();
  if (!user) {
    return res.status(401).json({
      success: false,
      message: '로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.'
    });
  }
  res.json({
    success: true,
    message: '로그인 되었습니다.',
    token: 'mocked-jwt-token',
    username: user.userId
  });
});

// 로그아웃
server.delete('/logout', (req, res) => {
  res.json({
    success: true,
    message: '로그아웃 되었습니다.'
  });
});

server.use(router);
server.listen(8080, () => {
  console.log('JSON Server is running on http://localhost:8080');
});